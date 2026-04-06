package com.witalo.digitalbank.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para resposta de autenticação contendo o token JWT.
 *
 * @author BankDash Team
 */
@Schema(description = "Authentication response DTO")
public record AuthResponseDTO(

        @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzM4NCJ9...")
        String token,

        @Schema(description = "Refresh token for obtaining new access token", example = "eyJhbGciOiJIUzM4NCJ9...")
        String refreshToken

) {
}