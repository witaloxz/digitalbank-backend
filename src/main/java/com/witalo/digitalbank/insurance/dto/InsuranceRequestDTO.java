package com.witalo.digitalbank.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitação de seguro de vida.
 *
 * @author BankDash Team
 */
@Schema(description = "Request DTO for life insurance application")
public record InsuranceRequestDTO(

        @Schema(description = "Selected insurance plan", example = "PREMIUM")
        @NotBlank(message = "Plan is required")
        String plan

) {
}