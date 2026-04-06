package com.witalo.digitalbank.user.exception;

import com.witalo.digitalbank.common.exception.BusinessException;

public class EmailNotFoundException extends BusinessException {
    public EmailNotFoundException(String email) {
        super("User not found for email: " + email);
    }
}