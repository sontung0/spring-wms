package nst.wms.auth.application;

import org.junit.jupiter.api.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceImplTest {

    private static final String PRIVATE_KEY_PEM = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDXe90MCU1RIizas8MOWYFFMHJKVeF0gr/keMaV6sZ7NjOdC1FAgqouSw+EhkyzmeK7CoP7ZKWdOvEDBiO7V2AaPPGlYWSfHc5zcLH+tyUsKGTCDDFvxcraRHYm0t7x12czx+IeVCjqIuLPFDpRLEdNvpe9S/JlJBqlfeQsEozvp3T/lSYCJQysxOcFNHCGzrSzElr1wlUTcJr6aQEL5iuo8B0ueJjrPK6DdOKDQ4GyFuEKFXKxnPAZw+O2lIRyNc2z/BFW+r/TkQP93B75hOh69uoX5rsaiDvW16XVPRPPdWFpga5vI7+LLy48ITZ1p9Rca+RAsZR302w9oWP3MafBAgMBAAECggEAbbWzoogt5EwJGESD4A90AJ0c43n8MszLFHDlcri420yKVL/JW5XrPWyb9vZFoP+Nb3t4HQtuQrIauVZr5Nsko9c5lOEUUOrlDONozG3EsEnUmvOITAchM6W9niCov+rSsYtoNdbVife/EH2U/3USzKNoMEm1nQcXxgBlpoa/WNgzOO7M61SE6UW+/x4i7A5CAc2L88EgQv+H8LZeELMPH7hPLszm6YFPFzuqg9rQjNjkzW77r4lnKGXks2hH+3JnSm8B62FQ2mWZiELYJc2BZPVgrDTgz4VILZGSlOEhqDbsSjFPzGeFFYcaVuQo1ZfoQ6q9L3QnNKNEzDHqH6cVnQKBgQDxe8BCL/0sL2f09V3TdSwgny5Qk+tEf27WvXc11XCE7eHZXgxKScrxhSHAIu7s4aLRXHnLxmTqWdYydqAgbSzlZk7y14VHWLFdKLo3JPvAilsBFzyyEn4UUTutkV/HVibzJFjr5T9f1fghlubVxCVIAkY78RHkVKsdxI/HXBYBVwKBgQDkb/+n++mXe26qmIyVbQSTWyV6o+e+feWl8jbkTOxH7x3FcZP7iXtucNe2dFRVKvmPpCBzOhTLODqfDIcWvTosR+Oa0OnRiqrWIg8ovXu6Yl4DZfjYMciezbUSfjjHTc7UZQR4YX7IJl0sgCrQFL6tQQczI5Eus0zQwHoFXbp4pwKBgBEjMirAUxxOpPcKbsUx/Ja4FUZcqQascIZG3e3xHtIgO7X8aazf/coUI6gKEqC3BqILCQ3AH6tOAtiD+Ks0dEo3b4TmbKv6jGPLHyQIuOEaMIksR/9vNolklnKj7YMtBwicXL8o/s1jVv+zQOCM0gSBPNDGM/nbVGWBbJ6+V0VzAoGBANrJKeKJs0ASR/u7/2ld8HPxaAP914bqrXgyc8BUrDh5G9pu3XycQEuWaAOnht3LOxoH58x0ZFX+lKagKQHuWjju4V71l/vJA7XzCPHMCSSJ8eDvWVY22B7AhhwgJ+E69E0YMx/YwMSRecuVNWv1cmodTff8qKBBueB1eGiLTH0hAoGBAO246esU6bIXDKM2MaNjsaZhFoTZD7mamYu5RD/J/UBcWB7OOL23p+IAdQW2NILU+Qkg0WLPQfCsqOETvxEA+9H20MwLYit9qOpf7+DidZdw+igTgARxJjunloQwlYuXbKpkh9NckSztt9XgXnjiGTUTzyRHjxui+BewwFgnHWBJ";

    private static final String PUBLIC_KEY_PEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA13vdDAlNUSIs2rPDDlmBRTBySlXhdIK/5HjGlerGezYznQtRQIKqLksPhIZMs5niuwqD+2SlnTrxAwYju1dgGjzxpWFknx3Oc3Cx/rclLChkwgwxb8XK2kR2JtLe8ddnM8fiHlQo6iLizxQ6USxHTb6XvUvyZSQapX3kLBKM76d0/5UmAiUMrMTnBTRwhs60sxJa9cJVE3Ca+mkBC+YrqPAdLniY6zyug3Tig0OBshbhChVysZzwGcPjtpSEcjXNs/wRVvq/05ED/dwe+YToevbqF+a7Gog71tel1T0Tz3VhaYGubyO/iy8uPCE2dafUXGvkQLGUd9NsPaFj9zGnwQIDAQAB";

    private final TokenServiceImpl tokenService = new TokenServiceImpl(
            "test-issuer",
            java.time.Duration.ofHours(24),
            loadPrivateKey(),
            loadPublicKey()
    );

    @Test
    void issue_shouldReturnJwtWithCorrectClaims() {
        String token = tokenService.issue(42L);

        assertNotNull(token);
        TokenService.TokenClaims claims = tokenService.verify(token);
        assertEquals(42L, claims.userId());
        assertEquals("test-issuer", claims.issuer());
        assertTrue(claims.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void verify_shouldThrowOnExpiredToken() {
        TokenServiceImpl expiredService = new TokenServiceImpl(
                "test-issuer",
                java.time.Duration.ofSeconds(-1),
                loadPrivateKey(),
                loadPublicKey()
        );

        String token = expiredService.issue(1L);

        assertThrows(Exception.class, () -> expiredService.verify(token));
    }

    @Test
    void verify_shouldThrowOnInvalidToken() {
        assertThrows(Exception.class, () -> tokenService.verify("invalid.token.here"));
    }

    @Test
    void verify_shouldThrowOnWrongIssuer() {
        TokenServiceImpl wrongIssuer = new TokenServiceImpl(
                "wrong-issuer",
                java.time.Duration.ofHours(24),
                loadPrivateKey(),
                loadPublicKey()
        );
        String token = wrongIssuer.issue(1L);

        assertThrows(Exception.class, () -> tokenService.verify(token));
    }

    private static RSAPrivateKey loadPrivateKey() {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(PRIVATE_KEY_PEM);
            java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(decoded);
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static RSAPublicKey loadPublicKey() {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(PUBLIC_KEY_PEM);
            java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(decoded);
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
