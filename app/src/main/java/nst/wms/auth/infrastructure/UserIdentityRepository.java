package nst.wms.auth.infrastructure;

import nst.wms.auth.domain.AuthUser;
import nst.wms.auth.domain.OAuthProviderCode;

public interface UserIdentityRepository {

    void save(Long userId, OAuthProviderCode provider, AuthUser authUser);
}
