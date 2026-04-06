package com.witalo.digitalbank.card.dto;

import com.witalo.digitalbank.card.enums.CardStatus;
import com.witalo.digitalbank.card.enums.CardType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para resposta de dados do cartão.
 *
 * @author BankDash Team
 */
@Schema(description = "Card response DTO")
public record CardResponseDTO(

        @Schema(description = "Card unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Card number (masked)", example = "**** **** **** 1234")
        String cardNumber,

        @Schema(description = "Card CVV", example = "123")
        String cvv,

        @Schema(description = "Card expiration date", example = "2025-12-31")
        LocalDate expiryDate,

        @Schema(description = "Card type", example = "CREDIT")
        CardType type,

        @Schema(description = "Card status", example = "ACTIVE")
        CardStatus status,

        @Schema(description = "Credit limit (only for credit cards)", example = "5000.00")
        BigDecimal creditLimit

) {
}