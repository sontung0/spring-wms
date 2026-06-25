package nst.wms.auth.presentation;

import nst.wms.auth.domain.IdpExchangeException;
import nst.wms.auth.domain.InvalidStateException;
import nst.wms.auth.domain.UnknownProviderException;
import nst.wms.common.api.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidStateException ex) {
        ErrorResponse response = new ErrorResponse("InvalidState", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IdpExchangeException.class)
    public ResponseEntity<ErrorResponse> handleIdpExchange(IdpExchangeException ex) {
        ErrorResponse response = new ErrorResponse("IdpError", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(UnknownProviderException.class)
    public ResponseEntity<ErrorResponse> handleUnknownProvider(UnknownProviderException ex) {
        ErrorResponse response = new ErrorResponse("UnknownProvider", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse response = new ErrorResponse("Unexpected", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
