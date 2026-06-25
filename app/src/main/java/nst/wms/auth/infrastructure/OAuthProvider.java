package nst.wms.auth.infrastructure;

import nst.wms.auth.domain.AuthUser;
import nst.wms.auth.domain.OAuthProviderCode;

public interface OAuthProvider {

    OAuthProviderCode getCode();

    String buildAuthorizationUrl(String state, String redirectUri);

    OAuthTokens exchangeCode(String code, String redirectUri);

    AuthUser fetchUserProfile(String accessToken);
}
