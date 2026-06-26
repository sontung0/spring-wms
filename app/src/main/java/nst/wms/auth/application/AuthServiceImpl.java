package nst.wms.auth.application;

import lombok.RequiredArgsConstructor;
import nst.wms.auth.domain.AuthUser;
import nst.wms.auth.domain.InvalidStateException;
import nst.wms.auth.domain.OAuthProviderCode;
import nst.wms.auth.infrastructure.*;
import nst.wms.user.application.UserService;
import nst.wms.user.application.UserUpdateData;
import nst.wms.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final OAuthProviderRegistry providerRegistry;
    private final OAuthProviderProperties oAuthProviderProperties;
    private final StateCache stateCache;
    private final TokenService tokenService;
    private final UserService userService;
    private final UserIdentityRepository userIdentityRepository;

    @Override
    public AuthorizeResponse authorize(String provider) {
        OAuthProvider oauthProvider = providerRegistry.resolve(provider);
        String state = UUID.randomUUID().toString();
        stateCache.put(state, provider);
        String redirectUri = oAuthProviderProperties.getRedirectUri();
        String authorizationUrl = oauthProvider.buildAuthorizationUrl(state, redirectUri);
        return new AuthorizeResponse(authorizationUrl);
    }

    @Override
    public AuthCallbackResponse callback(String code, String state) {
        String storedProvider = stateCache.getAndEvict(state);
        if (storedProvider == null) {
            throw new InvalidStateException();
        }

        OAuthProvider oauthProvider = providerRegistry.resolve(storedProvider);
        String redirectUri = oAuthProviderProperties.getRedirectUri();

        OAuthTokens tokens = oauthProvider.exchangeCode(code, redirectUri);
        AuthUser authUser = oauthProvider.fetchUserProfile(tokens.accessToken());

        UserUpdateData updateData = new UserUpdateData();
        updateData.name = authUser.getName();
        updateData.avatarUrl = authUser.getAvatarUrl();
        User user = userService.updateByEmail(authUser.getEmail(), updateData);

        userIdentityRepository.save(user.getId(), OAuthProviderCode.valueOf(storedProvider.toUpperCase()), authUser);

        String wmsToken = tokenService.issue(user.getId());
        return new AuthCallbackResponse(wmsToken);
    }
}
