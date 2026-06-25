package nst.wms.auth.application;

public interface AuthService {

    AuthorizeResponse authorize(String provider);

    AuthCallbackResponse callback(String provider, String code, String state);

    record AuthorizeResponse(String authorizationUrl) {
    }

    record AuthCallbackResponse(String accessToken) {
    }
}
