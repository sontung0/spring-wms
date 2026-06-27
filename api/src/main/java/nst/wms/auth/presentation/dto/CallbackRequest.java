package nst.wms.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "OAuth callback request containing authorization code and state")
public record CallbackRequest(
        @NotBlank(message = "Authorization code must not be blank")
        @Schema(description = "The authorization code received from the IdP", example = "4/0AX4XfWj...")
        String code,
        @NotBlank(message = "State must not be blank")
        @Schema(description = "The state parameter for CSRF protection (must match the state from /authorize)", example = "abc123xyz")
        String state) {
}
