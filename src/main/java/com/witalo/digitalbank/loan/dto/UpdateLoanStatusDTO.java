package com.witalo.digitalbank.loan.dto;

import com.witalo.digitalbank.loan.enums.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para atualização de status de empréstimo (uso administrativo).
 *
 * @author BankDash Team
 */
@Schema(description = "Request DTO for updating loan status")
public record UpdateLoanStatusDTO(

        @Schema(description = "New loan status", example = "ACTIVE")
        @NotNull(message = "Status is required")
        LoanStatus status

) {
}