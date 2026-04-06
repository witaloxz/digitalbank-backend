package com.witalo.digitalbank.account.dto;

import com.witalo.digitalbank.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO para requisição de criação de conta bancária.
 *
 * @author BankDash Team
 */
@Schema(description = "Create account request DTO")
public record CreateAccountRequestDTO(

        @Schema(description = "Account type", example = "CHECKING", required = true)
        @NotNull(message = "Account type is required")
        AccountType type,

        @Schema(description = "User ID associated with the account", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479", required = true)
        @NotNull(message = "User ID is required")
        UUID userId

) {
}