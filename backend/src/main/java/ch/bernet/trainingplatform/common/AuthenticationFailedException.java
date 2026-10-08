package ch.bernet.trainingplatform.common;

import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends DomainException {

    public AuthenticationFailedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

}
