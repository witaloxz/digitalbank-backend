package com.witalo.digitalbank.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

/**
 * DTO para requisição de criação de novo usuário.
 * Contém todas as validações necessárias para o cadastro.
 *
 * @author BankDash Team
 */
public record CreateUserRequestDTO(

        @Schema(description = "Full name of the user", example = "João Silva")
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 120, message = "Name must be between 3 and 120 characters")
        String name,

        @Schema(description = "Date of birth of user", example = "30/09/2005")
        @NotNull(message = "Date of birth is required")
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dateOfBirth,

        @Schema(description = "Email address (must be unique)", example = "joao@email.com")
        @NotBlank(message = "Email is required")
        @Email(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",
                message = "Invalid email format")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Schema(description = "Telephone is required", example = "+5511912345678")
        @NotBlank(message = "Telephone is required")
        @Pattern(regexp = "^\\+[1-9][0-9]{1,14}$", message = "Invalid phone number format. Use international format, e.g., +5511912345678")
        String phone,

        @Schema(description = "Brazilian CPF (11 digits, must be unique)", example = "12345678901")
        @NotBlank(message = "CPF is required")
        @CPF(message = "Invalid CPF")
        String cpf,

        @Schema(description = "User password (will be encrypted)", example = "mySecurePass123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password

) {
}