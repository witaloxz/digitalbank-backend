package com.witalo.digitalbank.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para visualização administrativa de solicitações de seguro.
 *
 * @author BankDash Team
 */
@Schema(description = "Admin insurance request view DTO")
public record AdminInsuranceDTO(

        @Schema(description = "Insurance request unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Name of the applicant", example = "João Silva")
        String userName,

        @Schema(description = "Email of the applicant", example = "joao@email.com")
        String userEmail,

        @Schema(description = "Selected insurance plan", example = "PREMIUM")
        String plan,

        @Schema(description = "Request status", example = "PENDING")
        String status,

        @Schema(description = "Request creation timestamp")
        LocalDateTime createdAt

) {
}