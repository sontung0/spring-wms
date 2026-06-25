package nst.wms.auth.domain;

import nst.wms.common.error.BusinessException;

public class InvalidStateException extends RuntimeException implements BusinessException {

    public InvalidStateException() {
        super("OAuth state has expired or is invalid");
    }
}
