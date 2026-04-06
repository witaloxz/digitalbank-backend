package com.witalo.digitalbank.account.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends AccountBusinessException {
    public InsufficientBalanceException(BigDecimal balance, BigDecimal amount) {
        super("Insufficient balance. Current: " + balance + ", requested: " + amount);
    }
}