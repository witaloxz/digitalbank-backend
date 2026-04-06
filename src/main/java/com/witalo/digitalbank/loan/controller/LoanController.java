package com.witalo.digitalbank.loan.controller;

import com.witalo.digitalbank.loan.dto.*;
import com.witalo.digitalbank.loan.service.LoanService;
import com.witalo.digitalbank.common.security.UserPrincipal;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsável pelos endpoints de gerenciamento de empréstimos.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Endpoints for loan management")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    /**
     * Lista empréstimos de uma conta
     * @param accountId ID da conta
     * @param pageable paginação
     * @return página de empréstimos
     */
    @GetMapping("/account/{accountId}")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #accountId, 'account', 'read')")
    @Operation(summary = "Get loans by account", description = "Returns paginated loans for a specific account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loans returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Page<LoanResponseDTO>> getLoansByAccount(
            @PathVariable UUID accountId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loanService.getLoansByAccount(accountId, pageable));
    }

    /**
     * Retorna resumo dos empréstimos de uma conta
     * @param accountId ID da conta
     * @return resumo dos empréstimos
     */
    @GetMapping("/account/{accountId}/summary")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #accountId, 'account', 'read')")
    @Operation(summary = "Get loan summary", description = "Returns a summary of loans for a specific account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<LoanSummaryDTO> getLoanSummary(@PathVariable UUID accountId) {
        return ResponseEntity.ok(loanService.getLoanSummary(accountId));
    }

    /**
     * Busca empréstimo por ID
     * @param loanId ID do empréstimo
     * @return dados do empréstimo
     */
    @GetMapping("/{loanId}")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #loanId, 'loan', 'read')")
    @Operation(summary = "Get loan by ID", description = "Returns a specific loan by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable UUID loanId) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    /**
     * Solicita um novo empréstimo
     * @param accountId ID da conta
     * @param dto dados da solicitação
     * @return empréstimo criado
     */
    @PostMapping("/account/{accountId}/request")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #accountId, 'account', 'write')")
    @Operation(summary = "Request a new loan", description = "Submits a loan request for approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan requested successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<LoanResponseDTO> requestLoan(
            @PathVariable UUID accountId,
            @Valid @RequestBody CreateLoanRequestDTO dto) {
        return ResponseEntity.ok(loanService.requestLoan(accountId, dto));
    }

    /**
     * Lista empréstimos pendentes (Admin)
     * @param pageable paginação
     * @return página de empréstimos pendentes
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending loans", description = "Returns all pending loans for admin approval. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending loans returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (requires ADMIN role)")
    })
    public ResponseEntity<Page<AdminLoanDTO>> getPendingLoans(Pageable pageable) {
        return ResponseEntity.ok(loanService.getPendingLoans(pageable));
    }

    /**
     * Aprova um empréstimo pendente (Admin)
     * @param loanId ID do empréstimo
     * @return empréstimo aprovado
     */
    @PatchMapping("/admin/{loanId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a loan", description = "Approves a pending loan. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan approved successfully"),
            @ApiResponse(responseCode = "400", description = "Loan cannot be approved"),
            @ApiResponse(responseCode = "403", description = "Access denied (requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<LoanResponseDTO> approveLoan(@PathVariable UUID loanId) {
        return ResponseEntity.ok(loanService.approveLoan(loanId));
    }

    /**
     * Rejeita um empréstimo pendente (Admin)
     * @param loanId ID do empréstimo
     */
    @PatchMapping("/admin/{loanId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a loan", description = "Rejects a pending loan. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Loan rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Loan cannot be rejected"),
            @ApiResponse(responseCode = "403", description = "Access denied (requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Void> rejectLoan(@PathVariable UUID loanId) {
        loanService.rejectLoan(loanId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca parcelas de um empréstimo
     * @param loanId ID do empréstimo
     * @param authentication dados de autenticação
     * @return lista de parcelas
     */
    @GetMapping("/{loanId}/installments")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #loanId, 'loan', 'read')")
    @Operation(summary = "Get loan installments", description = "Returns all installments for a specific loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Installments returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<List<InstallmentDTO>> getInstallments(@PathVariable UUID loanId, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID accountId = principal.getAccountId();
        return ResponseEntity.ok(loanService.getInstallmentsByLoan(loanId, accountId));
    }

    /**
     * Paga uma parcela via código de boleto
     * @param dto dados com código do boleto
     */
    @PostMapping("/installments/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Pay an installment", description = "Pays a loan installment using the boleto code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Installment paid successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid boleto code or insufficient balance"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Boleto not found")
    })
    public ResponseEntity<Void> payInstallment(@Valid @RequestBody PayInstallmentDTO dto) {
        loanService.payInstallment(dto.boletoCode());
        return ResponseEntity.noContent().build();
    }
}