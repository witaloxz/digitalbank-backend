package com.witalo.digitalbank.user.dto;

import com.witalo.digitalbank.user.enums.UserRole;
import com.witalo.digitalbank.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta contendo os dados completos do usuário.
 * Utilizado em consultas e retorno de operações de perfil.
 *
 * @author BankDash Team
 */
@Schema(description = "User response data")
public record UserResponseDTO(

        @Schema(description = "User unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Full name of the user", example = "João Silva")
        String name,

        @Schema(description = "Date of birth of user", example = "30/09/2005")
        LocalDate dateOfBirth,

        @Schema(description = "Email address", example = "joao@email.com")
        String email,

        @Schema(description = "Telephone of the user", example = "+5511912345678")
        String phone,

        @Schema(description = "Brazilian CPF", example = "12345678901")
        String cpf,

        @Schema(description = "Current status of the user account", example = "ACTIVE")
        UserStatus status,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp (null if never updated)")
        LocalDateTime updatedAt,

        @Schema(description = "User role in the system", example = "USER")
        UserRole role,

        @Schema(description = "User preferences")
        UserPreferencesDTO preferences

) {
}