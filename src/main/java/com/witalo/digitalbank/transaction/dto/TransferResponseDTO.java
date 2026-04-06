package com.witalo.digitalbank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de transferência realizada com sucesso.
 * Inclui informações da transação e do destinatário.
 *
 * @author BankDash Team
 */
@Schema(description = "Response DTO for completed transfer")
public record TransferResponseDTO(

        @Schema(description = "Transaction ID generated for the transfer", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID transactionId,

        @Schema(description = "Destination account number", example = "12345-6")
        String destinationAccountNumber,

        @Schema(description = "Full name of the destination account owner", example = "João Silva")
        String destinationOwnerName,

        @Schema(description = "Transfer amount", example = "100.00")
        BigDecimal amount,

        @Schema(description = "Transfer description", example = "Payment for services")
        String description,

        @Schema(description = "Transfer completion timestamp")
        LocalDateTime createdAt

) {
}