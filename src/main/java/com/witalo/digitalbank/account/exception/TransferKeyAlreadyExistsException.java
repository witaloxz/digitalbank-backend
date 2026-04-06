package com.witalo.digitalbank.account.exception;

import com.witalo.digitalbank.account.enums.TransferKeyType;

public class TransferKeyAlreadyExistsException extends TransferKeyBusinessException{
    public TransferKeyAlreadyExistsException(TransferKeyType type, String value) {
        super(String.format("Transfer key of type %s with value %s already exists", type, value));
    }
}