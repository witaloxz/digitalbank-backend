package com.witalo.digitalbank.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para dados mensais (usuários, transações, receita).
 *
 * @author BankDash Team
 */
@Schema(description = "Monthly data DTO for charts")
public record MonthlyDataDTO(

        @Schema(description = "Month name", example = "Jan")
        String month,

        @Schema(description = "Value for the month", example = "150")
        long value

) {
}