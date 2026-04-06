package com.witalo.digitalbank.common.security.filter;

import com.witalo.digitalbank.system.service.SystemSettingsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que verifica se o modo de manutenção está ativo.
 * Bloqueia requisições de usuários comuns durante a manutenção.
 * Admins continuam tendo acesso total.
 *
 * @author BankDash Team
 */
@Component
@RequiredArgsConstructor
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final SystemSettingsService settingsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        boolean maintenance = settingsService.isMaintenanceMode();

        if (maintenance) {
            String path = request.getRequestURI();

            // Endpoints permitidos durante manutenção
            boolean isAllowedPath = path.startsWith("/api/v1/admin") ||
                    path.startsWith("/api/v1/auth") ||
                    path.startsWith("/swagger-ui") ||
                    path.startsWith("/v3/api-docs") ||
                    path.equals("/actuator/health");

            if (!isAllowedPath) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = auth != null && auth.isAuthenticated() &&
                        auth.getAuthorities().stream()
                                .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));

                if (!isAdmin) {
                    response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Maintenance mode active. Please try again later.\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}