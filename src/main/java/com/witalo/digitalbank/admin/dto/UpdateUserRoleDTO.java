package com.witalo.digitalbank.admin.dto;

import com.witalo.digitalbank.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para atualização de role de usuário (admin).
 *
 * @author BankDash Team
 */
@Schema(description = "Update user role DTO")
public record UpdateUserRoleDTO(

        @Schema(description = "New user role", example = "ADMIN", required = true)
        @NotNull(message = "Role is required")
        UserRole role

) {
}