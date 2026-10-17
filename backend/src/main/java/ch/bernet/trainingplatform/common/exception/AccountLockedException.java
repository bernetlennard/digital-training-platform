package ch.bernet.trainingplatform.common.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends DomainException {

    public AccountLockedException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }

}
