package io.github.kjovik.expenseplatform.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException(String message) {
        super(message,HttpStatus.UNAUTHORIZED);
    }
}
