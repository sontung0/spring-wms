package nst.wms.user.domain;

import nst.wms.common.error.BusinessException;

public class UserNotFoundException extends RuntimeException implements BusinessException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
