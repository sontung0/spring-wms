package nst.wms.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the JWT access token")
public record CallbackResponse(
        @Schema(description = "JWT access token for authenticating subsequent API requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken) {
}
