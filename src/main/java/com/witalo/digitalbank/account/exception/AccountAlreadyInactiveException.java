package com.witalo.digitalbank.account.exception;

public class AccountAlreadyInactiveException extends AccountBusinessException {
    public AccountAlreadyInactiveException() {
        super("Account is already inactive");
    }
}