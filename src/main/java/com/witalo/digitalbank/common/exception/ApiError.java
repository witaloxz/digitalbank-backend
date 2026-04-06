package com.witalo.digitalbank.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Record para padronização de respostas de erro da API.
 *
 * @author BankDash Team
 */
@Schema(description = "Standard API error response")
public record ApiError(

        @Schema(description = "Timestamp of the error", example = "2024-01-15 10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status message", example = "Bad Request")
        String error,

        @Schema(description = "Detailed error message", example = "Email already exists")
        String message,

        @Schema(description = "Request path that caused the error", example = "/api/v1/users")
        String path

) {
}