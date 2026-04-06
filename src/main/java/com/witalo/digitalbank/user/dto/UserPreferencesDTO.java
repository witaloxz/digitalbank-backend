package com.witalo.digitalbank.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para transferência das preferências do usuário.
 * Utilizado em respostas da API para consulta de preferências.
 *
 * @author BankDash Team
 */
public record UserPreferencesDTO(

        @Schema(description = "User language preference", example = "pt-br")
        String language,

        @Schema(description = "Email notifications enabled", example = "true")
        boolean emailNotifications,

        @Schema(description = "SMS notifications enabled", example = "false")
        boolean smsNotifications,

        @Schema(description = "Push notifications enabled", example = "true")
        boolean pushNotifications,

        @Schema(description = "Two-factor authentication enabled", example = "false")
        boolean twoFactorEnabled

) {
}