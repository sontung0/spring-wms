# Authentication Design Spec

## 1. Overview

The WMS API will support OAuth2 Authorization Code flow for authentication. The SPA delegates all IdP interaction to the backend — it only ever holds a WMS-issued JWT. The backend handles the IdP code exchange, upserts the user record, and issues its own short-lived access token.

The design is provider-agnostic: a new IdP is added by implementing one interface (`OAuthProvider`) and registering a property block in `application.properties`.

## 2. API Endpoints

| Method | Path | Auth required | Purpose |
|---|---|---|---|
| `GET` | `/auth/authorize?provider={provider}` | No | Returns the IdP authorization URL for the SPA to redirect to |
| `POST` | `/auth/callback` | No | Receives `{ code, state, provider }`, exchanges with IdP, upserts user, issues WMS JWT |

## 3. Login Flow

1. SPA calls `GET /auth/authorize?provider=google`
2. App generates a random `state` UUID, stores it in the state cache (5-min TTL), builds the IdP authorization URL (with `state` and `redirect_uri` from config), returns it to the SPA
3. SPA redirects the user's browser to the IdP URL
4. User authenticates at IdP; IdP redirects back to SPA with `?code=...&state=...`
5. SPA calls `POST /auth/callback` with `{ code, state, provider }`
6. App validates `state` against cache (CSRF protection), evicts it
7. App exchanges `code` with IdP token endpoint using `RestClient` → receives IdP access token + id token
8. App fetches user profile from IdP using `RestClient` (email, name, avatar URL)
9. App calls `userService.updateByEmail(email, { name, avatarUrl })` → upserts `users` table, returns `User` with internal `id`
10. App saves `user_identities` record (auth module owns this table) to link the provider identity to the user
11. App issues WMS JWT (`sub` = `users.id`, `iat`, `exp`) and returns `{ accessToken }` to the SPA
12. SPA stores the token; uses `Authorization: Bearer <accessToken>` on all subsequent API calls

## 4. Module Structure

A new `auth` Spring Modulith module at `nst.wms.auth`:

```
nst.wms.auth
├── presentation
│   └── AuthController
├── application          (@NamedInterface)
│   ├── AuthService
│   ├── TokenService
│   └── OAuthProviderService
├── domain
│   ├── AuthUser            (IdP profile: providerUserId, email, name, avatarUrl)
│   └── OAuthProviderCode   (enum: GOOGLE, GITHUB)
└── infrastructure
    ├── OAuthProvider               (interface — IdP port)
    ├── GoogleOAuthProvider         (implements OAuthProvider)
    ├── GitHubOAuthProvider         (implements OAuthProvider)
    ├── JwtTokenProvider            (RSA-signed JWT issue/verify)
    ├── UserIdentitiesRepository    (JPA — auth module owns this table)
    └── StateCache                  (interface + Caffeine implementation)
```

**Module dependency:** `auth` → `user` (one-way). `auth` calls `userService.updateByEmail()` via the `user` module's `@NamedInterface`. `user` has no dependency on `auth`. Spring Modulith verification will enforce this.

## 5. Interface Designs

### 5.1 application layer

```java
package nst.wms.auth.application;

@NamedInterface
public interface AuthService {
    AuthorizeResponse authorize(String provider);
    AuthCallbackResponse callback(String provider, String code, String state);
}

@NamedInterface
public interface TokenService {
    String issue(Long userId);
    TokenClaims verify(String token);
}

public interface OAuthProviderService {
    OAuthProvider resolve(String providerCode);
}
```

### 5.2 infrastructure layer

```java
package nst.wms.auth.infrastructure;

public interface OAuthProvider {
    OAuthProviderCode getCode();
    String buildAuthorizationUrl(String state, String redirectUri);
    OAuthTokens exchangeCode(String code, String redirectUri);
    AuthUser fetchUserProfile(String accessToken);
}

// Implementations: GoogleOAuthProvider, GitHubOAuthProvider
```

### 5.3 DTOs / value objects

```java
// Returned by GET /auth/authorize
public record AuthorizeResponse(String authorizationUrl) {}

// Returned by POST /auth/callback
public record AuthCallbackResponse(String accessToken) {}

// Returned by TokenService.verify()
public record TokenClaims(Long userId, Instant expiresAt) {}

// Returned by OAuthProvider.exchangeCode()
public record OAuthTokens(String accessToken, String idToken) {}

// OAuth state store (implementation: Caffeine with TTL)
public interface StateCache {
    void put(String state, String provider);
    String getAndEvict(String state);
}
```

### 5.4 AuthUser domain object

```java
package nst.wms.auth.domain;

public class AuthUser {
    private String providerUserId;
    private String email;
    private String name;
    private String avatarUrl;
    // getters, setters, constructor
}
```

## 6. Database Schema

### 6.1 New table: `user_identities`

Links an IdP identity to an internal user.

```sql
CREATE TABLE user_identities (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    provider         VARCHAR(50)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    name             VARCHAR(255),
    avatar_url       VARCHAR(512),
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    UNIQUE (provider, provider_user_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 6.2 Users table change

Add a new column:

```sql
ALTER TABLE users ADD COLUMN email VARCHAR(255) UNIQUE;
```

## 7. Token Design

**Access token (JWT)**

- Algorithm: RS256 (RSA private key signs, public key verifies)
- Lifetime: configurable via `auth.jwt.ttl` (default `PT24H` — ISO-8601 duration)
- Claims: `sub` (users.id), `iss` (wms), `iat`, `exp`
- Validated stateless by Spring Security on every `/api/**` request — no DB lookup

## 8. State Cache

Spring `CacheManager` abstraction with Caffeine as the default backend:

- Cache name: `oauth-states`
- TTL: 5 minutes
- Key: state UUID, Value: provider name
- `@CachePut` on `/auth/authorize`, `@CacheEvict` on `/auth/callback`
- Swappable to Redis by adding `spring-boot-starter-data-redis` and one property change — no code changes

## 9. Security Configuration

- Spring Security filter chain configured as a **resource server** (JWT validation)
- `/auth/**` → `permitAll`
- `/api/**` → `authenticated`
- JWT public key loaded from config (`auth.jwt.public-key`)
- RSA key pair stored in `application.properties` (not rotated automatically in v1)

## 10. UserService.updateByEmail() Contract

Added to the `user` module's `@NamedInterface`:

```java
package nst.wms.user.application;

public interface UserService {
    User updateByEmail(String email, UpdateUserByEmailRequest request);
    // ... existing methods
}

public record UpdateUserByEmailRequest(
    String name,
    String avatarUrl
) {}
```

Behavior:

1. Look up `users` by `email`
2. If found → update only non-null fields (`name`, `avatarUrl`); return `User`
3. If not found → create new `User` with the given email and non-null fields

## 11. OAuth Client Implementation

No external dependency — uses Spring's built-in `RestClient` (available since Spring 6.1, on classpath via `spring-boot-starter-webmvc`).

Each `OAuthProvider` implementation handles two HTTP calls:

1. `exchangeCode(code, redirectUri)` → `POST` to IdP token endpoint
2. `fetchUserProfile(accessToken)` → `GET` to IdP user info endpoint

Example for Google:
```java
@Component
public class GoogleOAuthProvider implements OAuthProvider {
    private final RestClient restClient;
    // ...
}
```

Adding a new provider = one new class implementing `OAuthProvider` + `client-id` and `client-secret` in config. All URLs are pre-defined as constants in the provider implementation, overridable via config if needed.

## 12. Configuration

```properties
# OAuth
auth.oauth.redirectUri=http://localhost:3000/auth/callback

# Google (URLs are pre-defined as defaults in code)
auth.oauth.providers.google.client-id=...
auth.oauth.providers.google.client-secret=...

# JWT
auth.jwt.ttl=PT24H
auth.jwt.private-key=...
auth.jwt.public-key=...
auth.jwt.issuer=wms
```

## 13. Error Handling

All errors follow the existing `ErrorResponse` format (`error` in PascalCase, human-readable `message`).

| Scenario | HTTP Status | Error code | Message |
|---|---|---|---|
| Invalid/expired state | `400` | `InvalidState` | "OAuth state has expired or is invalid" |
| IdP code exchange failure | `502` | `IdpError` | "Failed to exchange authorization code with provider" |
| User denied consent | `400` | `AccessDenied` | "Access was denied by the identity provider" |
| Expired/invalid access token | `401` | `InvalidToken` | "Access token is invalid or has expired" |
| Unknown provider | `400` | `UnknownProvider` | "Unknown authentication provider: {provider}" |
| Unexpected error | `500` | `Unexpected` | "An unexpected error occurred" |

## 14. Testing Strategy

- **`TokenServiceTest`** — unit test JWT sign/verify, expiry enforcement, claim extraction, configurable TTL
- **`AuthServiceTest`** — unit test callback flow with mocked `OAuthProvider` and `UserService`
- **`AuthControllerTest`** (`@WebMvcTest`) — test both endpoints, following `UserApiTest` pattern
- **`GoogleOAuthProviderTest`** — unit test URL building and token exchange with mocked `RestClient`
- **`ModulithVerificationTest`** — existing test automatically verifies `auth` → `user` dependency is one-way
