package com.witalo.digitalbank.account.dto;

import com.witalo.digitalbank.account.enums.TransferKeyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO para resposta de chave de transferência (Pix).
 *
 * @author BankDash Team
 */
@Schema(description = "Transfer key response DTO")
public record TransferKeyResponseDTO(

        @Schema(description = "Transfer key unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,

        @Schema(description = "Transfer key type", example = "EMAIL")
        TransferKeyType type,

        @Schema(description = "Transfer key value", example = "usuario@email.com")
        String value

) {
}