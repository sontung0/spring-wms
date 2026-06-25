package nst.wms.auth.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "auth.oauth")
public class OAuthProviderProperties {

    private String redirectUri = "http://localhost:3000/auth/callback";
    private Map<String, ProviderConfig> providers = new HashMap<>();

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public Map<String, ProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, ProviderConfig> providers) {
        this.providers = providers;
    }

    public ProviderConfig getProvider(String code) {
        ProviderConfig config = providers.get(code.toLowerCase());
        if (config == null) {
            throw new IllegalArgumentException("No config for provider: " + code);
        }
        return config;
    }

    public static class ProviderConfig {
        private String clientId;
        private String clientSecret;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }
}
