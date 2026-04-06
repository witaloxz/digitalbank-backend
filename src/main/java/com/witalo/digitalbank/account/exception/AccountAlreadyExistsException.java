package com.witalo.digitalbank.account.exception;

import java.util.UUID;

public class AccountAlreadyExistsException extends AccountBusinessException {
    public AccountAlreadyExistsException(UUID userId) {
        super("User already has an account: " + userId);
    }
}