package com.witalo.digitalbank.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de atualização de senha do usuário.
 * Requer senha atual, nova senha e confirmação.
 *
 * @author BankDash Team
 */
public record UpdatePasswordRequestDTO(

        @Schema(description = "Current password", required = true, example = "myCurrentPass123")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New password", required = true, example = "myNewSecurePass456")
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        String newPassword,

        @Schema(description = "Confirmation of the new password", required = true, example = "myNewSecurePass456")
        @NotBlank(message = "Password confirmation is required")
        String confirmNewPassword

) {
}