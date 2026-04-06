package com.witalo.digitalbank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * DTO para resposta de contatos recentes em transferências.
 * Utilizado na tela de Quick Transfer do dashboard.
 *
 * @author BankDash Team
 */
@Schema(description = "Recent contact information for quick transfers")
public record RecentContactDTO(

        @Schema(description = "Account ID of the contact", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID accountId,

        @Schema(description = "Full name of the contact", example = "João Silva")
        String name,

        @Schema(description = "Account number for transfer", example = "12345-6")
        String accountNumber,

        @Schema(description = "Initial letter for avatar display", example = "JS")
        String avatarLetter

) {
}