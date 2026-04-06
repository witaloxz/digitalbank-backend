package com.witalo.digitalbank.transaction.dto;

import com.witalo.digitalbank.account.enums.TransferKeyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para requisição de transferência entre contas.
 * Suporta transferência via número da conta ou chave Pix (email, telefone, CPF).
 *
 * @author BankDash Team
 */
@Schema(description = "Request DTO for money transfer between accounts")
public record TransferRequestDTO(

        @Schema(description = "Source account ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        @NotNull(message = "Source account ID is required")
        UUID fromAccountId,

        @Schema(description = "Destination account number (6 digits)", example = "000001")
        String destinationAccountNumber,

        @Schema(description = "Transfer key (email, phone or CPF)", example = "user@example.com")
        String transferKey,

        @Schema(description = "Type of the transfer key (required if transferKey is provided)", example = "EMAIL")
        TransferKeyType transferKeyType,

        @Schema(description = "Transfer amount", example = "100.00")
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @Schema(description = "Optional description", example = "Payment for services")
        String description

) {
}