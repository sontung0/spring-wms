package nst.wms.auth.application;

import nst.wms.auth.domain.*;
import nst.wms.auth.infrastructure.OAuthProvider;
import nst.wms.auth.infrastructure.OAuthProviderRegistry;
import nst.wms.auth.infrastructure.OAuthTokens;
import nst.wms.auth.infrastructure.StateCache;
import nst.wms.auth.infrastructure.UserIdentityRepository;
import nst.wms.auth.infrastructure.OAuthProviderProperties;
import nst.wms.user.application.UserService;
import nst.wms.user.application.UserUpdateData;
import nst.wms.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private OAuthProviderRegistry providerRegistry;
    @Mock
    private StateCache stateCache;
    @Mock
    private TokenService tokenService;
    @Mock
    private UserService userService;
    @Mock
    private UserIdentityRepository userIdentityRepository;
    @Mock
    private OAuthProviderProperties oAuthProviderProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void authorize_shouldReturnAuthorizationUrl() {
        OAuthProvider provider = mock(OAuthProvider.class);
        when(providerRegistry.resolve("GOOGLE")).thenReturn(provider);
        when(provider.buildAuthorizationUrl(anyString(), anyString()))
                .thenReturn("https://accounts.google.com/o/oauth2/auth?...");
        when(oAuthProviderProperties.getRedirectUri()).thenReturn("http://localhost:3000/auth/callback");

        AuthService.AuthorizeResponse response = authService.authorize("GOOGLE");

        assertNotNull(response);
        assertTrue(response.authorizationUrl().startsWith("https://"));
        verify(stateCache).put(anyString(), eq("GOOGLE"));
    }

    @Test
    void callback_shouldExchangeCodeAndIssueToken() {
        when(stateCache.getAndEvict("valid-state")).thenReturn("GOOGLE");

        OAuthProvider provider = mock(OAuthProvider.class);
        when(providerRegistry.resolve("GOOGLE")).thenReturn(provider);
        when(provider.exchangeCode("auth-code", "http://localhost:3000/auth/callback"))
                .thenReturn(new OAuthTokens("idp-token", "id-token"));
        when(provider.fetchUserProfile("idp-token"))
                .thenReturn(new AuthUser("123", "user@example.com", "Test User", "https://avatar.url"));

        when(userService.updateByEmail(eq("user@example.com"), any(UserUpdateData.class)))
                .thenReturn(new User(1L, "Test User", "user@example.com", "https://avatar.url", LocalDateTime.now(), LocalDateTime.now()));
        when(tokenService.issue(1L)).thenReturn("wms-jwt-token");
        when(oAuthProviderProperties.getRedirectUri()).thenReturn("http://localhost:3000/auth/callback");

        AuthService.AuthCallbackResponse response = authService.callback("auth-code", "valid-state");

        assertNotNull(response);
        assertEquals("wms-jwt-token", response.accessToken());
        verify(userIdentityRepository).save(eq(1L), eq(OAuthProviderCode.GOOGLE), any(AuthUser.class));
    }

    @Test
    void callback_shouldThrowOnInvalidState() {
        when(stateCache.getAndEvict("bad-state")).thenReturn(null);

        assertThrows(InvalidStateException.class,
                () -> authService.callback("code", "bad-state"));
    }
}
