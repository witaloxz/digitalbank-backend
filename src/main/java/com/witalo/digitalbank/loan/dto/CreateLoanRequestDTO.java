package com.witalo.digitalbank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para solicitação de criação de empréstimo.
 *
 * @author BankDash Team
 */
@Schema(description = "Request DTO for creating a new loan")
public record CreateLoanRequestDTO(

        @Schema(description = "Loan name/title", example = "Personal Loan")
        @NotBlank(message = "Loan name is required")
        String name,

        @Schema(description = "Loan amount", example = "10000.00")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1000.00", message = "Minimum loan amount is R$ 1,000")
        BigDecimal amount,

        @Schema(description = "Interest rate percentage", example = "5.5")
        @NotNull(message = "Interest rate is required")
        @DecimalMin(value = "0.1", message = "Interest rate must be greater than 0")
        BigDecimal interestRate,

        @Schema(description = "Loan term in months", example = "12")
        @NotNull(message = "Term months is required")
        @DecimalMin(value = "6", message = "Minimum term is 6 months")
        Integer termMonths

) {
}