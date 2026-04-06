package com.witalo.digitalbank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de dados do empréstimo.
 *
 * @author BankDash Team
 */
@Schema(description = "Loan response DTO")
public record LoanResponseDTO(

        @Schema(description = "Loan unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Loan name/title", example = "Personal Loan")
        String name,

        @Schema(description = "Total loan amount", example = "10000.00")
        BigDecimal totalAmount,

        @Schema(description = "Remaining amount to pay", example = "7500.00")
        BigDecimal remainingAmount,

        @Schema(description = "Interest rate percentage", example = "5.5")
        BigDecimal interestRate,

        @Schema(description = "Monthly payment amount", example = "850.00")
        BigDecimal monthlyPayment,

        @Schema(description = "Loan due date")
        LocalDateTime dueDate,

        @Schema(description = "Loan status", example = "ACTIVE")
        String status,

        @Schema(description = "Progress percentage of payment", example = "25")
        Integer progressPercentage

) {
}