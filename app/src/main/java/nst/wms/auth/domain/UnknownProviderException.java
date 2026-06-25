package nst.wms.auth.domain;

import nst.wms.common.error.BusinessException;

public class UnknownProviderException extends RuntimeException implements BusinessException {

    public UnknownProviderException(String provider) {
        super("Unknown authentication provider: " + provider);
    }
}
