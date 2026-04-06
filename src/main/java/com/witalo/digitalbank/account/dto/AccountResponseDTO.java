package com.witalo.digitalbank.account.dto;

import com.witalo.digitalbank.account.enums.AccountStatus;
import com.witalo.digitalbank.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de dados da conta bancária.
 *
 * @author BankDash Team
 */
@Schema(description = "Account response DTO")
public record AccountResponseDTO(

        @Schema(description = "Account unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Bank agency", example = "0001")
        String agency,

        @Schema(description = "Account number", example = "12345")
        String accountNumber,

        @Schema(description = "Account digit", example = "6")
        String accountDigit,

        @Schema(description = "Account type", example = "CHECKING")
        AccountType type,

        @Schema(description = "Current balance", example = "1250.00")
        BigDecimal balance,

        @Schema(description = "Account status", example = "ACTIVE")
        AccountStatus status,

        @Schema(description = "User ID associated with the account", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID userId,

        @Schema(description = "Account creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt

) {
}