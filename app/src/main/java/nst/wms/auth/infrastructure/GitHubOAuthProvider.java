package nst.wms.auth.infrastructure;

import nst.wms.auth.domain.AuthUser;
import nst.wms.auth.domain.IdpExchangeException;
import nst.wms.auth.domain.OAuthProviderCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class GitHubOAuthProvider implements OAuthProvider {

    private static final Logger log = LoggerFactory.getLogger(GitHubOAuthProvider.class);

    private static final String AUTHORIZATION_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_INFO_URL = "https://api.github.com/user";

    private final OAuthProviderProperties properties;
    private final RestClient restClient;

    public GitHubOAuthProvider(OAuthProviderProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public OAuthProviderCode getCode() {
        return OAuthProviderCode.GITHUB;
    }

    @Override
    public String buildAuthorizationUrl(String state, String redirectUri) {
        OAuthProviderProperties.ProviderConfig config = properties.getProvider("github");
        return AUTHORIZATION_URL
                + "?client_id=" + encode(config.getClientId())
                + "&redirect_uri=" + encode(redirectUri)
                + "&scope=user:email"
                + "&state=" + encode(state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public OAuthTokens exchangeCode(String code, String redirectUri) {
        OAuthProviderProperties.ProviderConfig config = properties.getProvider("github");
        try {
            Map<String, Object> body = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Accept", "application/json")
                    .body("grant_type=authorization_code"
                            + "&code=" + encode(code)
                            + "&redirect_uri=" + encode(redirectUri)
                            + "&client_id=" + encode(config.getClientId())
                            + "&client_secret=" + encode(config.getClientSecret()))
                    .retrieve()
                    .body(Map.class);

            return new OAuthTokens(
                    (String) body.get("access_token"),
                    null
            );
        } catch (Exception e) {
            log.error("Failed to exchange authorization code with GitHub", e);
            throw new IdpExchangeException("Failed to exchange authorization code with provider", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public AuthUser fetchUserProfile(String accessToken) {
        try {
            Map<String, Object> profile = restClient.get()
                    .uri(USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(Map.class);

            String email = (String) profile.get("email");
            if (email == null) {
                email = (String) profile.get("login") + "@github.local";
            }

            return new AuthUser(
                    String.valueOf(profile.get("id")),
                    email,
                    (String) profile.get("name"),
                    (String) profile.get("avatar_url")
            );
        } catch (Exception e) {
            log.error("Failed to fetch user profile from GitHub", e);
            throw new IdpExchangeException("Failed to fetch user profile from provider", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
