package com.witalo.digitalbank.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para requisição de atualização parcial de dados do usuário.
 * Todos os campos são opcionais, permitindo atualização seletiva.
 *
 * @author BankDash Team
 */
public record UpdateUserRequestDTO(

        @Schema(description = "New name of the user (optional)", example = "João Santos")
        @Size(min = 3, max = 120, message = "Name must be between 3 and 120 characters")
        String name,

        @Schema(description = "New email of the user (optional)", example = "joao@email.com")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Schema(description = "New date of birth (optional)", example = "15/05/1990")
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dateOfBirth,

        @Schema(description = "New telephone number (optional)", example = "+5511912345678")
        @Pattern(regexp = "^\\+[1-9][0-9]{1,14}$", message = "Invalid phone number format. Use international format, e.g., +5511912345678")
        String phone,

        @Schema(description = "New password (optional)", example = "newPass123")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password

) {
}