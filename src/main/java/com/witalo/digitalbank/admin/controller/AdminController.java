package com.witalo.digitalbank.admin.controller;

import com.witalo.digitalbank.admin.dto.*;
import com.witalo.digitalbank.admin.service.AdminService;
import com.witalo.digitalbank.loan.dto.UpdateLoanStatusDTO;
import com.witalo.digitalbank.transaction.dto.AdminTransactionResponseDTO;
import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;
import com.witalo.digitalbank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsável pelos endpoints administrativos do sistema.
 * Requer role ADMIN para todos os endpoints.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative endpoints")
public class AdminController {

    private final AdminService adminService;
    private final TransactionService transactionService;

    // ============================
    // ESTATÍSTICAS DO DASHBOARD
    // ============================

    @GetMapping("/stats")
    @Operation(summary = "Dashboard statistics", description = "Returns main statistics for admin dashboard.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/stats/users-monthly")
    @Operation(summary = "Monthly user registrations", description = "Returns user registrations grouped by month.")
    public ResponseEntity<List<MonthlyDataDTO>> getMonthlyUsers(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(adminService.getMonthlyUserRegistrations(months));
    }

    @GetMapping("/stats/transactions-monthly")
    @Operation(summary = "Monthly transactions", description = "Returns transaction count grouped by month.")
    public ResponseEntity<List<MonthlyDataDTO>> getMonthlyTransactions(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(adminService.getMonthlyTransactions(months));
    }

    @GetMapping("/stats/revenue-monthly")
    @Operation(summary = "Monthly revenue", description = "Returns revenue grouped by month.")
    public ResponseEntity<List<MonthlyDataDTO>> getMonthlyRevenue(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(adminService.getMonthlyRevenue(months));
    }

    @GetMapping("/stats/status-distribution")
    @Operation(summary = "Transaction status distribution", description = "Returns distribution of transaction statuses.")
    public ResponseEntity<List<StatusDistributionDTO>> getStatusDistribution() {
        return ResponseEntity.ok(adminService.getTransactionStatusDistribution());
    }

    // ============================
    // GERENCIAMENTO DE USUÁRIOS
    // ============================

    @GetMapping("/users")
    @Operation(summary = "List all users", description = "Returns paginated list of users with optional filters.")
    public ResponseEntity<Page<AdminUserResponseDTO>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.findAllUsers(search, status, role, pageable));
    }

    @PatchMapping("/users/{userId}/toggle-status")
    @Operation(summary = "Toggle user status", description = "Activates or deactivates a user account.")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable UUID userId) {
        adminService.toggleUserStatus(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Update user role", description = "Changes a user's role (USER/ADMIN).")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleDTO dto) {
        adminService.updateUserRole(userId, dto.role());
        return ResponseEntity.noContent().build();
    }

    // ============================
    // GERENCIAMENTO DE TRANSAÇÕES
    // ============================

    @GetMapping("/transactions")
    @Operation(summary = "List all transactions", description = "Returns paginated list of transactions with filters.")
    public ResponseEntity<Page<AdminTransactionResponseDTO>> listAdminTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAdminTransactions(search, type, status, pageable));
    }

    @PatchMapping("/transactions/{id}/status")
    @Operation(summary = "Update transaction status", description = "Approves or rejects a pending transaction.")
    public ResponseEntity<Void> updateTransactionStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionStatusDTO dto) {
        transactionService.updateTransactionStatus(id, dto.status());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/transactions/{id}/reverse")
    @Operation(summary = "Reverse transaction", description = "Reverses a completed transaction (admin only).")
    public ResponseEntity<Void> reverseTransaction(@PathVariable UUID id) {
        transactionService.reverseTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // ============================
    // GERENCIAMENTO DE EMPRÉSTIMOS
    // ============================

    @GetMapping("/loans")
    @Operation(summary = "List all loans", description = "Returns paginated list of loans with optional status filter.")
    public ResponseEntity<Page<AdminLoanDTO>> listLoans(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllLoans(status, pageable));
    }

    @PatchMapping("/loans/{loanId}/status")
    @Operation(summary = "Update loan status", description = "Updates the status of a loan (approve/reject).")
    public ResponseEntity<Void> updateLoanStatus(
            @PathVariable UUID loanId,
            @RequestBody @Valid UpdateLoanStatusDTO dto) {
        adminService.updateLoanStatus(loanId, dto.status());
        return ResponseEntity.noContent().build();
    }
}