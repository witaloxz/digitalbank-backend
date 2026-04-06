package com.witalo.digitalbank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para pagamento de parcela via código de boleto.
 *
 * @author BankDash Team
 */
@Schema(description = "Request DTO for paying an installment via boleto code")
public record PayInstallmentDTO(

        @Schema(description = "Boleto code for the installment", example = "12345678901234567890123456789012345678901234")
        @NotBlank(message = "Boleto code is required")
        String boletoCode

) {
}