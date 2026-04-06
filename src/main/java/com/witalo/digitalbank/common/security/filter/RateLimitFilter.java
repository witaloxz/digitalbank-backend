package com.witalo.digitalbank.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para controle de limite de requisições (rate limiting).
 * Ignora endpoints de WebSocket e autenticação.
 *
 * @author BankDash Team
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // TODO: Implementar lógica de rate limit
        filterChain.doFilter(request, response);
    }

    /**
     * Define quais requisições não devem passar pelo filtro de rate limit
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/ws-notifications") || path.startsWith("/api/v1/auth");
    }
}