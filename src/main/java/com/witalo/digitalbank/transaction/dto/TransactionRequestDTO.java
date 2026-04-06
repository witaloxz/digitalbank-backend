package com.witalo.digitalbank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO base para requisições de transações financeiras.
 * Utilizado para depósitos e saques.
 *
 * @author BankDash Team
 */
@Schema(description = "Request DTO for financial transactions (deposit/withdraw)")
public record TransactionRequestDTO(

        @Schema(description = "Account ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        @NotNull(message = "Account ID is required")
        UUID accountId,

        @Schema(description = "Transaction amount", example = "150.00")
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @Schema(description = "Optional description", example = "Salary deposit")
        String description

) {
}