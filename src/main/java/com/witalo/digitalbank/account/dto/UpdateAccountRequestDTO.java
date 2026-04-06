package com.witalo.digitalbank.account.dto;

import com.witalo.digitalbank.account.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisição de atualização de status da conta.
 *
 * @author BankDash Team
 */
@Schema(description = "Update account status request DTO")
public record UpdateAccountRequestDTO(

        @Schema(description = "New status for the account", example = "ACTIVE", required = true)
        @NotNull(message = "Account status is required")
        AccountStatus status

) {
}