package nst.wms.auth.application;

import java.time.Instant;

public interface TokenService {

    String issue(Long userId);

    TokenClaims verify(String token);

    record TokenClaims(Long userId, String issuer, Instant expiresAt) {
    }
}
