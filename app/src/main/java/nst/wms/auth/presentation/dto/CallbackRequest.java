package nst.wms.auth.presentation.dto;

public record CallbackRequest(String provider, String code, String state) {
}
