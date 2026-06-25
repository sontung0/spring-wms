package nst.wms.auth.domain;

public class UnknownProviderException extends RuntimeException {

    public UnknownProviderException(String provider) {
        super("Unknown authentication provider: " + provider);
    }
}
