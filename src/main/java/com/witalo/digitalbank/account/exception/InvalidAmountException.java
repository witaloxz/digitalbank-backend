package com.witalo.digitalbank.account.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends AccountBusinessException {
    public InvalidAmountException(BigDecimal amount) {
        super("Invalid amount: " + amount);
    }
}