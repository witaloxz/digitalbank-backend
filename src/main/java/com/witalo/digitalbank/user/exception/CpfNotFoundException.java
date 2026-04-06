package com.witalo.digitalbank.user.exception;

import com.witalo.digitalbank.common.exception.BusinessException;

public class CpfNotFoundException extends BusinessException {
    public CpfNotFoundException(String cpf) {
        super("User not found for CPF: " + cpf);
    }
}