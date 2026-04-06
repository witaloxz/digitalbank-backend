package com.witalo.digitalbank.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO para estatísticas do painel administrativo.
 *
 * @author BankDash Team
 */
@Schema(description = "Admin dashboard statistics DTO")
public record AdminStatsDTO(

        @Schema(description = "Total number of registered users", example = "1250")
        long totalUsers,

        @Schema(description = "Total number of transactions", example = "8750")
        long totalTransactions,

        @Schema(description = "Number of pending loan requests", example = "15")
        long totalPendingLoans,

        @Schema(description = "Total revenue from fees", example = "12500.00")
        BigDecimal totalRevenue

) {
}