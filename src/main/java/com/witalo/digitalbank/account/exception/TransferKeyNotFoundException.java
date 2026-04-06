package com.witalo.digitalbank.account.exception;

import com.witalo.digitalbank.account.enums.TransferKeyType;

public class TransferKeyNotFoundException extends TransferKeyBusinessException {
    public TransferKeyNotFoundException(TransferKeyType type, String value) {
        super(String.format("Transfer key of type %s with value %s not found", type, value));
    }
}