package br.com.microservice.statelessauthapi.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionGlobalHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionDetails details = new ExceptionDetails(status.value(), exception.getMessage());
        return new ResponseEntity<>(details, status);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ExceptionDetails details = new ExceptionDetails(status.value(), exception.getMessage());
        return new ResponseEntity<>(details, status);
    }
}
