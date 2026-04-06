package com.witalo.digitalbank.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de usuários no painel administrativo.
 *
 * @author BankDash Team
 */
@Schema(description = "Admin user response DTO")
public record AdminUserResponseDTO(

        @Schema(description = "User unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Full name of the user", example = "João Silva")
        String name,

        @Schema(description = "Email address", example = "joao@email.com")
        String email,

        @Schema(description = "Brazilian CPF", example = "12345678901")
        String cpf,

        @Schema(description = "User role", example = "USER")
        String role,

        @Schema(description = "Account status", example = "ACTIVE")
        String status,

        @Schema(description = "Account balance", example = "1250.00")
        BigDecimal balance,

        @Schema(description = "User registration date")
        LocalDateTime joinDate

) {
}