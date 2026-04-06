package com.witalo.digitalbank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO para resumo consolidado de empréstimos do usuário.
 *
 * @author BankDash Team
 */
@Schema(description = "Loan summary DTO for user dashboard")
public record LoanSummaryDTO(

        @Schema(description = "Total amount of all active loans", example = "25000.00")
        BigDecimal totalLoans,

        @Schema(description = "Total monthly payment amount", example = "2100.00")
        BigDecimal monthlyPayment,

        @Schema(description = "Average interest rate across loans", example = "6.5")
        BigDecimal avgInterestRate

) {
}