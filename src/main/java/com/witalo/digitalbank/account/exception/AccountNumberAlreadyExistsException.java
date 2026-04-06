package com.witalo.digitalbank.account.exception;

public class AccountNumberAlreadyExistsException extends AccountBusinessException {
    public AccountNumberAlreadyExistsException(String accountNumber) {
        super("Account number already exists: " + accountNumber);
    }
}