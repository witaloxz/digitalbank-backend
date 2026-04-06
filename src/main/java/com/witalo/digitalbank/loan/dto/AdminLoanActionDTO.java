package com.witalo.digitalbank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para ação administrativa sobre empréstimo (aprovar/rejeitar).
 *
 * @author BankDash Team
 */
@Schema(description = "Admin action DTO for loan approval/rejection")
public record AdminLoanActionDTO(

        @Schema(description = "Whether the loan is approved", example = "true")
        @NotNull(message = "Approved status is required")
        boolean approved

) {
}