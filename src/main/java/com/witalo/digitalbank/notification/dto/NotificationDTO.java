package com.witalo.digitalbank.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de notificações do usuário.
 *
 * @author BankDash Team
 */
@Schema(description = "Notification response DTO")
public record NotificationDTO(

        @Schema(description = "Notification unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Notification title", example = "Transfer received")
        String title,

        @Schema(description = "Notification message", example = "You received R$150.00 from João Silva")
        String message,

        @Schema(description = "Notification type", example = "TRANSFER_RECEIVED")
        String type,

        @Schema(description = "Read status", example = "false")
        boolean read,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt

) {
}