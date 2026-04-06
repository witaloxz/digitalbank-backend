package com.witalo.digitalbank.account.exception;

public class SameAccountTransferException extends AccountBusinessException {
    public SameAccountTransferException() {
        super("Transfer to the same account is not allowed");
    }
}