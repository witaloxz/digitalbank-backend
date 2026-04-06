package com.witalo.digitalbank.account.dto;

import com.witalo.digitalbank.account.enums.TransferKeyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisição de criação de chave de transferência (Pix).
 *
 * @author BankDash Team
 */
@Schema(description = "Create transfer key request DTO")
public record CreateTransferKeyRequestDTO(

        @Schema(description = "Transfer key type", example = "EMAIL", required = true)
        @NotNull(message = "Key type is required")
        TransferKeyType type,

        @Schema(description = "Transfer key value", example = "usuario@email.com", required = true)
        @NotBlank(message = "Key value is required")
        String value

) {
}