package com.witalo.digitalbank.account.dto;

import com.witalo.digitalbank.transaction.dto.TransactionResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO para extrato da conta bancária.
 *
 * @author BankDash Team
 */
@Schema(description = "Account statement response DTO")
public record AccountStatementDTO(

        @Schema(description = "Account ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID accountId,

        @Schema(description = "Current account balance", example = "1250.00")
        BigDecimal balance,

        @Schema(description = "List of transactions")
        List<TransactionResponseDTO> transactions

) {
}