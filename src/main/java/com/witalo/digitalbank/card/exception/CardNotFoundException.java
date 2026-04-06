package com.witalo.digitalbank.card.exception;

import java.util.UUID;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(UUID id) {
        super("Card not found with id: " + id);
    }
}