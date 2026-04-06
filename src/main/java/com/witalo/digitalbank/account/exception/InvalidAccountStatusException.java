package com.witalo.digitalbank.account.exception;

public class InvalidAccountStatusException extends AccountBusinessException {
    public InvalidAccountStatusException() {
        super("Invalid account status");
    }
}