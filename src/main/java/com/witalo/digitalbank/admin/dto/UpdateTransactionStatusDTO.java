package com.witalo.digitalbank.admin.dto;

import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para atualização de status de transação (admin).
 *
 * @author BankDash Team
 */
@Schema(description = "Update transaction status DTO")
public record UpdateTransactionStatusDTO(

        @Schema(description = "New transaction status", example = "SUCCESS", required = true)
        @NotNull(message = "Status is required")
        TransactionStatus status

) {
}