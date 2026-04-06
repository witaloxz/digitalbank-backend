package com.witalo.digitalbank.card.dto;

import com.witalo.digitalbank.card.enums.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para requisição de criação de novo cartão.
 *
 * @author BankDash Team
 */
@Schema(description = "Request DTO for creating a new card")
public record CreateCardRequestDTO(

        @Schema(description = "Card type", example = "CREDIT", required = true)
        @NotNull(message = "Card type is required")
        CardType type,

        @Schema(description = "Credit limit (required for credit cards)", example = "5000.00")
        BigDecimal creditLimit

) {
}