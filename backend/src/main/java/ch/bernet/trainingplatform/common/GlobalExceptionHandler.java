package ch.bernet.trainingplatform.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        HttpStatus status = ex.getStatus();
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), ex.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // TODO: 400 mit Feldname + Meldung je Fehler in details, nie den abgelehnten Wert (#NFA-1.1)
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<String> validationErrors = ex.getBindingResult().getFieldErrors()
                .stream().map(fe -> "Validation error: " + fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), "Validation error", validationErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        // TODO: Fehler loggen (L11), 500 ohne Stacktrace/interne Details zurückgeben (L10)
        String errorMessage = "Unexepcted Internal Server Error";
        log.error(errorMessage, ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status.value())
                .body(new ErrorResponse(status.value(), errorMessage, List.of()));
    }

}
