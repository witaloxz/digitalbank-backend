package com.witalo.digitalbank.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para distribuição de status de transações (gráfico de pizza).
 *
 * @author BankDash Team
 */
@Schema(description = "Status distribution DTO for pie chart")
public record StatusDistributionDTO(

        @Schema(description = "Status name", example = "COMPLETED")
        String name,

        @Schema(description = "Count of transactions with this status", example = "850")
        long value,

        @Schema(description = "Color for chart display", example = "#4CAF50")
        String color

) {
}