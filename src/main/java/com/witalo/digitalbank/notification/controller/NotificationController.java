package com.witalo.digitalbank.notification.controller;

import com.witalo.digitalbank.notification.dto.NotificationDTO;
import com.witalo.digitalbank.notification.service.NotificationService;
import com.witalo.digitalbank.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável pelos endpoints de notificações do usuário.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Busca as notificações do usuário autenticado
     * @param authentication dados de autenticação
     * @param pageable paginação
     * @return página de notificações
     */
    @GetMapping
    @Operation(summary = "Get user notifications", description = "Returns paginated notifications for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<NotificationDTO>> getMyNotifications(
            Authentication authentication,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUserNotifications(principal.getId(), pageable));
    }

    /**
     * Retorna a quantidade de notificações não lidas do usuário
     * @param authentication dados de autenticação
     * @return quantidade de notificações não lidas
     */
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notifications count", description = "Returns the number of unread notifications for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUnreadCount(principal.getId()));
    }

    /**
     * Marca todas as notificações do usuário como lidas
     * @param authentication dados de autenticação
     */
    @PatchMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications as read for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All notifications marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.noContent().build();
    }
}