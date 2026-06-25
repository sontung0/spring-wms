package nst.wms.auth.domain;

import nst.wms.common.error.BusinessException;

public class IdpExchangeException extends RuntimeException implements BusinessException {

    public IdpExchangeException(String message) {
        super(message);
    }

    public IdpExchangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
