package com.witalo.digitalbank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para resposta de parcelas de empréstimo.
 *
 * @author BankDash Team
 */
@Schema(description = "Installment response DTO")
public record InstallmentDTO(

        @Schema(description = "Installment unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Installment number", example = "1")
        Integer installmentNumber,

        @Schema(description = "Installment amount", example = "850.00")
        BigDecimal amount,

        @Schema(description = "Due date", example = "2024-01-15")
        LocalDate dueDate,

        @Schema(description = "Boleto code for payment", example = "12345678901234567890123456789012345678901234")
        String boletoCode,

        @Schema(description = "Payment status", example = "PENDING")
        String status,

        @Schema(description = "Payment date (if paid)", example = "2024-01-10")
        LocalDate paidAt

) {
}