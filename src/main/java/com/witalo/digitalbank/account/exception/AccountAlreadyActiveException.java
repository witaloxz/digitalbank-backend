package com.witalo.digitalbank.account.exception;

public class AccountAlreadyActiveException extends AccountBusinessException {
    public AccountAlreadyActiveException() {
        super("Account is already active");
    }
}