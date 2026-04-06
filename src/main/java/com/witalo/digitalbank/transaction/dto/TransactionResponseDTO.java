package com.witalo.digitalbank.transaction.dto;

import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de transações financeiras.
 * Inclui informações completas da transação e indicador se pode ser revertida.
 *
 * @author BankDash Team
 */
@Schema(description = "Response DTO for transaction data")
public record TransactionResponseDTO(

        @Schema(description = "Transaction unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Account ID associated with the transaction", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID accountId,

        @Schema(description = "Type of transaction", example = "DEPOSIT")
        TransactionType transactionType,

        @Schema(description = "Current status of the transaction", example = "SUCCESS")
        TransactionStatus transactionStatus,

        @Schema(description = "Transaction amount", example = "150.00")
        BigDecimal amount,

        @Schema(description = "Account balance after the transaction", example = "1250.00")
        BigDecimal balanceAfter,

        @Schema(description = "Transaction description", example = "Salary deposit")
        String description,

        @Schema(description = "Associated transfer ID (if applicable)", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID transferId,

        @Schema(description = "Transaction creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Indicates if the transaction can be reversed", example = "true")
        boolean canReverse

) {
}