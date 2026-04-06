package com.witalo.digitalbank.card.exception;

public class InvalidCreditLimitException extends RuntimeException {

    public InvalidCreditLimitException(String message) {
        super(message);
    }

    public InvalidCreditLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}