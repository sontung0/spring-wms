package nst.wms.auth.domain;

public class InvalidStateException extends RuntimeException {

    public InvalidStateException() {
        super("OAuth state has expired or is invalid");
    }
}
