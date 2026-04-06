package com.witalo.digitalbank.transaction.controller;

import com.witalo.digitalbank.common.security.UserPrincipal;
import com.witalo.digitalbank.transaction.dto.RecentContactDTO;
import com.witalo.digitalbank.transaction.dto.TransferRequestDTO;
import com.witalo.digitalbank.transaction.dto.TransferResponseDTO;
import com.witalo.digitalbank.transaction.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsável pelos endpoints de transferências entre contas.
 * Suporta transferências via número da conta ou chave Pix, com idempotência.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Endpoints for account-to-account transfers")
public class TransferController {

    private final TransferService transferService;

    /**
     * Realiza uma transferência entre contas
     * @param dto dados da transferência (origem, destino, valor)
     * @param idempotencyKey chave única para evitar duplicação
     * @return dados da transferência realizada
     */
    @PostMapping
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #dto.fromAccountId(), 'account', 'write')")
    @Operation(summary = "Execute a transfer", description = "Transfers funds between two accounts. Idempotency key is required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful (or already processed due to idempotency)"),
            @ApiResponse(responseCode = "400", description = "Invalid data, insufficient balance or inactive account"),
            @ApiResponse(responseCode = "403", description = "Access denied (not the owner of the source account)")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TransferResponseDTO> transfer(
            @Valid @RequestBody TransferRequestDTO dto,
            @Parameter(description = "Unique key for idempotency (recommended: UUID)", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        TransferResponseDTO response = transferService.transfer(dto, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Reverte uma transferência (estorno)
     * @param id ID da transferência a ser revertida
     * @param userDetails dados do usuário autenticado
     */
    @PostMapping("/{id}/reverse")
    @PreAuthorize("@transferService.isDestination(authentication, #id)")
    @Operation(summary = "Reverse a transfer", description = "Reverses a completed transfer. Only the recipient can reverse it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transfer successfully reversed"),
            @ApiResponse(responseCode = "400", description = "Transfer cannot be reversed (invalid status or incomplete)"),
            @ApiResponse(responseCode = "403", description = "Access denied (not the recipient)"),
            @ApiResponse(responseCode = "404", description = "Transfer not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> reverseTransfer(
            @Parameter(description = "ID of the transfer to be reversed", required = true)
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        transferService.reverseTransfer(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca contatos recentes para transferência rápida
     * @param authentication dados de autenticação
     * @return lista de contatos recentes (máximo 5)
     */
    @GetMapping("/contacts/recent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recent contacts", description = "Returns the 5 most recent transfer contacts for quick transfer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent contacts returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<RecentContactDTO>> getRecentContacts(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID accountId = principal.getAccountId();
        return ResponseEntity.ok(transferService.getRecentContacts(accountId));
    }
}