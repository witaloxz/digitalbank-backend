package com.witalo.digitalbank.account.controller;

import com.witalo.digitalbank.account.dto.AccountResponseDTO;
import com.witalo.digitalbank.account.dto.AccountStatementDTO;
import com.witalo.digitalbank.account.dto.CreateAccountRequestDTO;
import com.witalo.digitalbank.account.dto.UpdateAccountRequestDTO;
import com.witalo.digitalbank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller responsável pelos endpoints de gerenciamento de contas bancárias.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Endpoints for managing bank accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    /**
     * Cria uma nova conta bancária
     * @param dto dados da conta
     * @return conta criada
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #dto.userId == principal.id")
    @Operation(summary = "Create a new account", description = "Creates an account for a user. The system automatically generates the account number and digit.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or user already has an account"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Account number conflict or user already has an account")
    })
    public ResponseEntity<AccountResponseDTO> create(@Valid @RequestBody CreateAccountRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.create(dto));
    }

    /**
     * Lista todas as contas com paginação (Admin)
     * @param pageable paginação
     * @return página de contas
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all accounts", description = "Returns a page of all accounts. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of accounts returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<AccountResponseDTO>> findAll(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(accountService.findAllPaged(pageable));
    }

    /**
     * Retorna o extrato da conta
     * @param id ID da conta
     * @return extrato com saldo e transações
     */
    @GetMapping("/{id}/statement")
    @PreAuthorize("hasRole('ADMIN') or @accountService.isAccountOwner(#id, authentication)")
    @Operation(summary = "Get account statement", description = "Returns the statement with current balance and list of transactions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statement retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AccountStatementDTO> statement(
            @Parameter(description = "Account ID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getStatement(id));
    }

    /**
     * Busca conta por ID
     * @param id ID da conta
     * @return dados da conta
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountService.isAccountOwner(#id, authentication)")
    @Operation(summary = "Find account by ID", description = "Returns the details of a specific account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AccountResponseDTO> findById(
            @Parameter(description = "Account ID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.findById(id));
    }

    /**
     * Busca conta por ID do usuário
     * @param userId ID do usuário
     * @return dados da conta
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @Operation(summary = "Find account by user ID", description = "Returns the account associated with a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AccountResponseDTO> findByUserId(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.findByUserId(userId));
    }

    /**
     * Busca conta pelo número da conta (Admin)
     * @param accountNumber número da conta
     * @return dados da conta
     */
    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Find account by number", description = "Returns an account by its number. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AccountResponseDTO> findByAccountNumber(
            @Parameter(description = "Account number (6 digits)", required = true) @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.findByAccountNumber(accountNumber));
    }

    /**
     * Atualiza o status da conta (Admin)
     * @param id ID da conta
     * @param dto dados com novo status
     * @return conta atualizada
     */
    @PatchMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status", description = "Activates or deactivates an account. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or account already has the provided status"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AccountResponseDTO> update(
            @Parameter(description = "Account ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequestDTO dto) {
        return ResponseEntity.ok(accountService.update(id, dto));
    }
}