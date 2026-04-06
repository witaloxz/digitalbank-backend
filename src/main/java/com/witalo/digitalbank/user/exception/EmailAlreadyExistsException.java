package com.witalo.digitalbank.user.exception;

import com.witalo.digitalbank.common.exception.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}