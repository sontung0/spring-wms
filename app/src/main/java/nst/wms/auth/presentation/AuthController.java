package nst.wms.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nst.wms.auth.application.AuthService;
import nst.wms.auth.presentation.dto.AuthorizeResponse;
import nst.wms.auth.presentation.dto.CallbackRequest;
import nst.wms.auth.presentation.dto.CallbackResponse;
import nst.wms.common.api.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "OAuth2 authentication endpoints")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/authorize")
    @Operation(
            summary = "Get IdP authorization URL",
            description = "Initiates the OAuth2 flow by returning the authorization URL for the specified identity provider. " +
                    "The returned URL includes a state parameter for CSRF protection. " +
                    "Redirect the user to this URL to begin authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authorization URL generated successfully"),
            @ApiResponse(responseCode = "400", description = "Unknown or unsupported provider",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthorizeResponse> authorize(
            @Parameter(description = "OAuth provider identifier (e.g., GOOGLE, GITHUB)", example = "GOOGLE", required = true)
            @RequestParam String provider) {
        AuthService.AuthorizeResponse result = authService.authorize(provider);
        return ResponseEntity.ok(new AuthorizeResponse(result.authorizationUrl()));
    }

    @PostMapping("/callback")
    @Operation(
            summary = "OAuth callback",
            description = "Completes the OAuth2 flow by exchanging the authorization code for a WMS JWT access token. " +
                    "The provider is determined from the state parameter stored during the /authorize call. " +
                    "On success, a new user is created or an existing user is updated based on the IdP profile."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, JWT returned"),
            @ApiResponse(responseCode = "400", description = "Invalid state (expired or tampered) or unknown provider",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Identity provider exchange error (IdP unreachable or returned error)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CallbackResponse> callback(@RequestBody CallbackRequest request) {
        AuthService.AuthCallbackResponse result = authService.callback(
                request.code(), request.state());
        return ResponseEntity.ok(new CallbackResponse(result.accessToken()));
    }
}
