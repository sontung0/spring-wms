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
public class GoogleOAuthProvider implements OAuthProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthProvider.class);

    private static final String AUTHORIZATION_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    private final OAuthProviderProperties properties;
    private final RestClient restClient;

    public GoogleOAuthProvider(OAuthProviderProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public OAuthProviderCode getCode() {
        return OAuthProviderCode.GOOGLE;
    }

    @Override
    public String buildAuthorizationUrl(String state, String redirectUri) {
        OAuthProviderProperties.ProviderConfig config = properties.getProvider("google");
        return AUTHORIZATION_URL
                + "?client_id=" + encode(config.getClientId())
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&state=" + encode(state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public OAuthTokens exchangeCode(String code, String redirectUri) {
        OAuthProviderProperties.ProviderConfig config = properties.getProvider("google");
        try {
            Map<String, Object> body = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("grant_type=authorization_code"
                            + "&code=" + encode(code)
                            + "&redirect_uri=" + encode(redirectUri)
                            + "&client_id=" + encode(config.getClientId())
                            + "&client_secret=" + encode(config.getClientSecret()))
                    .retrieve()
                    .body(Map.class);

            return new OAuthTokens(
                    (String) body.get("access_token"),
                    (String) body.get("id_token")
            );
        } catch (Exception e) {
            log.error("Failed to exchange authorization code with Google", e);
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
                    .retrieve()
                    .body(Map.class);

            return new AuthUser(
                    (String) profile.get("id"),
                    (String) profile.get("email"),
                    (String) profile.get("name"),
                    (String) profile.get("picture")
            );
        } catch (Exception e) {
            log.error("Failed to fetch user profile from Google", e);
            throw new IdpExchangeException("Failed to fetch user profile from provider", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
