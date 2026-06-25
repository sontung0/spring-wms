package nst.wms.auth.domain;

public class IdpExchangeException extends RuntimeException {

    public IdpExchangeException(String message) {
        super(message);
    }

    public IdpExchangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
