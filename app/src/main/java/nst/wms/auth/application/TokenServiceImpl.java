package nst.wms.auth.application;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class TokenServiceImpl implements TokenService {

    private final String issuer;
    private final Duration ttl;
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    @Autowired
    public TokenServiceImpl(
            @Value("${auth.jwt.issuer:wms}") String issuer,
            @Value("${auth.jwt.ttl:PT24H}") Duration ttl,
            @Value("${auth.jwt.private-key}") String privateKeyPem,
            @Value("${auth.jwt.public-key}") String publicKeyPem) {
        this.issuer = issuer;
        this.ttl = ttl;
        this.privateKey = parsePrivateKey(privateKeyPem);
        this.publicKey = parsePublicKey(publicKeyPem);
    }

    // Constructor for testing
    public TokenServiceImpl(String issuer, Duration ttl, RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.issuer = issuer;
        this.ttl = ttl;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    public String issue(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                claims
        );

        try {
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    @Override
    public TokenClaims verify(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            boolean valid = signedJWT.verify(new RSASSAVerifier(publicKey));
            if (!valid) {
                throw new RuntimeException("Invalid JWT signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (!issuer.equals(claims.getIssuer())) {
                throw new RuntimeException("Invalid JWT issuer");
            }

            Date expTime = claims.getExpirationTime();
            if (expTime == null || expTime.before(Date.from(Instant.now()))) {
                throw new RuntimeException("JWT has expired");
            }

            return new TokenClaims(
                    Long.parseLong(claims.getSubject()),
                    claims.getIssuer(),
                    expTime.toInstant()
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private RSAPrivateKey parsePrivateKey(String pem) {
        try {
            String cleaned = pem
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = java.util.Base64.getDecoder().decode(cleaned);
            java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(decoded);
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse RSA private key", e);
        }
    }

    private RSAPublicKey parsePublicKey(String pem) {
        try {
            String cleaned = pem
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = java.util.Base64.getDecoder().decode(cleaned);
            java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(decoded);
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse RSA public key", e);
        }
    }
}
