package com.witalo.digitalbank.account.exception;

public class AccountStatusAlreadyException extends AccountBusinessException {
    public AccountStatusAlreadyException(String message) {
        super(message);
    }
}