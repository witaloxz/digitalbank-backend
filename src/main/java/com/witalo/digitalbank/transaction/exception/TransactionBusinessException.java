package com.witalo.digitalbank.transaction.exception;

public class TransactionBusinessException extends RuntimeException {
    public TransactionBusinessException(String message) {
        super(message);
    }
}
