package com.witalo.digitalbank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de transações no painel administrativo.
 *
 * @author BankDash Team
 */
@Schema(description = "Admin transaction response DTO")
public record AdminTransactionResponseDTO(

        @Schema(description = "Transaction unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Sender name or source", example = "João Silva")
        String from,

        @Schema(description = "Recipient name or destination", example = "Maria Santos")
        String to,

        @Schema(description = "Transaction amount", example = "150.00")
        BigDecimal amount,

        @Schema(description = "Transaction type", example = "transfer")
        String type,

        @Schema(description = "Transaction status", example = "completed")
        String status,

        @Schema(description = "Transaction date and time")
        LocalDateTime date

) {
}