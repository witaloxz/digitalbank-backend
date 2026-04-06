package com.witalo.digitalbank.user.exception;

import com.witalo.digitalbank.common.exception.BusinessException;
import java.util.UUID;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(UUID id) {
        super("User not found with id: " + id);
    }
}