package com.witalo.digitalbank.account.exception;

public class AccountInactiveException extends AccountBusinessException {
    public AccountInactiveException() {
        super("Operation not allowed. Account is inactive");
    }
}