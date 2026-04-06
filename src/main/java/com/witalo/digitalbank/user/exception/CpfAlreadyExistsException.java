package com.witalo.digitalbank.user.exception;

import com.witalo.digitalbank.common.exception.BusinessException;

public class CpfAlreadyExistsException extends BusinessException {
    public CpfAlreadyExistsException(String cpf) {
        super("CPF already registered: " + cpf);
    }
}