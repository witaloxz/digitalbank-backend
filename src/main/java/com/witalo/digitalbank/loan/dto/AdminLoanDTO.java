package com.witalo.digitalbank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para visualização administrativa de empréstimos pendentes.
 *
 * @author BankDash Team
 */
@Schema(description = "Admin loan view DTO for pending loans")
public record AdminLoanDTO(

        @Schema(description = "Loan unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Name of the borrower", example = "João Silva")
        String userName,

        @Schema(description = "Email of the borrower", example = "joao@email.com")
        String userEmail,

        @Schema(description = "Loan name/title", example = "Personal Loan")
        String loanName,

        @Schema(description = "Total loan amount", example = "10000.00")
        BigDecimal totalAmount,

        @Schema(description = "Interest rate percentage", example = "5.5")
        BigDecimal interestRate,

        @Schema(description = "Progress percentage of payment", example = "30")
        Integer progressPercentage,

        @Schema(description = "Loan status", example = "PENDING")
        String status,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt

) {
}