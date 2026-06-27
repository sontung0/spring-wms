package nst.wms.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the IdP authorization URL")
public record AuthorizeResponse(
        @Schema(description = "The URL to redirect the user to for IdP authorization", example = "https://accounts.google.com/o/oauth2/auth?client_id=...&redirect_uri=...&state=...")
        String authorizationUrl) {
}
