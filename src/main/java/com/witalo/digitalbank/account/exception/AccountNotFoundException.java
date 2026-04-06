package com.witalo.digitalbank.account.exception;

import java.util.UUID;

public class AccountNotFoundException extends AccountBusinessException {
    public AccountNotFoundException(UUID id) {
        super("Account not found with id: " + id);
    }
    public AccountNotFoundException(String accountNumber) {
        super("Account not found with number: " + accountNumber);
    }
}