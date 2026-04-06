package com.witalo.digitalbank.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Serviço responsável pela geração e validação de tokens JWT.
 * Gerencia criação, extração de claims e validação de expiração.
 *
 * @author BankDash Team
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera um token JWT para o usuário autenticado
     * @param userDetails dados do usuário
     * @return token JWT
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof UserPrincipal principal) {
            claims.put("userId", principal.getId());
            claims.put("accountId", principal.getAccountId());
            claims.put("role", principal.getAuthorities().iterator().next().getAuthority());
        }
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida se o token é válido para o usuário
     * @param token token JWT
     * @param userDetails dados do usuário
     * @return true se válido, false caso contrário
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extrai o username (email) do token
     * @param token token JWT
     * @return email do usuário
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai a data de expiração do token
     * @param token token JWT
     * @return data de expiração
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai o ID do usuário do token
     * @param token token JWT
     * @return UUID do usuário
     */
    public UUID extractUserId(String token) {
        return extractClaim(token, claims -> UUID.fromString(claims.get("userId", String.class)));
    }

    /**
     * Extrai o ID da conta do token
     * @param token token JWT
     * @return UUID da conta
     */
    public UUID extractAccountId(String token) {
        return extractClaim(token, claims -> UUID.fromString(claims.get("accountId", String.class)));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}