package nst.wms.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import nst.wms.auth.application.AuthService;
import nst.wms.auth.presentation.dto.AuthorizeResponse;
import nst.wms.auth.presentation.dto.CallbackRequest;
import nst.wms.auth.presentation.dto.CallbackResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "OAuth2 authentication endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/authorize")
    @Operation(summary = "Get IdP authorization URL", description = "Returns the authorization URL for the specified provider")
    public ResponseEntity<AuthorizeResponse> authorize(@RequestParam String provider) {
        AuthService.AuthorizeResponse result = authService.authorize(provider);
        return ResponseEntity.ok(new AuthorizeResponse(result.authorizationUrl()));
    }

    @PostMapping("/callback")
    @Operation(summary = "OAuth callback", description = "Exchanges authorization code for WMS JWT")
    public ResponseEntity<CallbackResponse> callback(@RequestBody CallbackRequest request) {
        AuthService.AuthCallbackResponse result = authService.callback(
                request.provider(), request.code(), request.state());
        return ResponseEntity.ok(new CallbackResponse(result.accessToken()));
    }
}
