package com.witalo.digitalbank.transaction.controller;

import com.witalo.digitalbank.transaction.dto.TransactionRequestDTO;
import com.witalo.digitalbank.transaction.dto.TransactionResponseDTO;
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

import java.util.UUID;

/**
 * Controller responsável pelos endpoints de transações financeiras.
 * Gerencia depósitos, saques e consultas de transações.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for deposits, withdrawals and transaction queries")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Realiza um depósito em conta
     * @param dto dados da transação (accountId, amount, description)
     * @return transação realizada
     */
    @PostMapping("/deposit")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #dto.accountId(), 'account', 'write')")
    @Operation(summary = "Make a deposit", description = "Adds funds to the specified account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit successful"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or inactive account"),
            @ApiResponse(responseCode = "403", description = "Access denied (not the account owner)")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TransactionResponseDTO> deposit(@Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(transactionService.deposit(dto));
    }

    /**
     * Realiza um saque em conta
     * @param dto dados da transação (accountId, amount, description)
     * @return transação realizada
     */
    @PostMapping("/withdraw")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #dto.accountId(), 'account', 'write')")
    @Operation(summary = "Make a withdrawal", description = "Withdraws funds from the specified account, respecting limits and balance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal successful"),
            @ApiResponse(responseCode = "400", description = "Invalid amount, insufficient balance or inactive account"),
            @ApiResponse(responseCode = "403", description = "Access denied (not the account owner)")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TransactionResponseDTO> withdraw(@Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(transactionService.withdraw(dto));
    }

    /**
     * Busca transação por ID
     * @param id identificador da transação
     * @return dados da transação encontrada
     */
    @GetMapping("/{id}")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #id, 'transaction', 'read')")
    @Operation(summary = "Find transaction by ID", description = "Returns details of a specific transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "403", description = "Access denied (transaction does not belong to the user)")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TransactionResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.findById(id));
    }

    /**
     * Lista todas as transações com paginação e filtros (apenas ADMIN)
     * @param search termo de busca
     * @param type tipo da transação
     * @param status status da transação
     * @param pageable parâmetros de paginação
     * @return página de transações
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all transactions", description = "Returns a page of all transactions. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of transactions returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (requires ADMIN role)")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<TransactionResponseDTO>> findAllPaged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAllPaged(search, type, status, pageable));
    }
}