package com.witalo.digitalbank.account.controller;

import com.witalo.digitalbank.account.dto.CreateTransferKeyRequestDTO;
import com.witalo.digitalbank.account.dto.TransferKeyResponseDTO;
import com.witalo.digitalbank.account.service.TransferKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsável pelos endpoints de gerenciamento de chaves de transferência (Pix).
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/accounts/{accountId}/keys")
@RequiredArgsConstructor
@Tag(name = "Transfer Keys", description = "Manage Pix-like keys for accounts")
@SecurityRequirement(name = "bearerAuth")
public class TransferKeyController {

    private final TransferKeyService transferKeyService;

    /**
     * Cria uma nova chave de transferência para a conta
     * @param accountId ID da conta
     * @param dto dados da chave (tipo e valor)
     * @return chave criada
     */
    @PostMapping
    @PreAuthorize("@accountService.isAccountOwner(#accountId, authentication)")
    @Operation(summary = "Create a new transfer key", description = "Adds a new Pix key to the account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer key created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid key type or value"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Transfer key already exists")
    })
    public ResponseEntity<TransferKeyResponseDTO> createKey(
            @PathVariable UUID accountId,
            @Valid @RequestBody CreateTransferKeyRequestDTO dto) {
        return ResponseEntity.ok(transferKeyService.create(accountId, dto));
    }

    /**
     * Lista todas as chaves de transferência da conta
     * @param accountId ID da conta
     * @return lista de chaves
     */
    @GetMapping
    @PreAuthorize("@accountService.isAccountOwner(#accountId, authentication)")
    @Operation(summary = "List all keys of an account", description = "Returns all Pix keys associated with the account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Keys returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<List<TransferKeyResponseDTO>> listKeys(@PathVariable UUID accountId) {
        return ResponseEntity.ok(transferKeyService.listByAccount(accountId));
    }

    /**
     * Remove uma chave de transferência
     * @param accountId ID da conta
     * @param keyId ID da chave
     */
    @DeleteMapping("/{keyId}")
    @PreAuthorize("@accountService.isAccountOwner(#accountId, authentication)")
    @Operation(summary = "Delete a transfer key", description = "Removes a Pix key from the account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transfer key deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account or key not found")
    })
    public ResponseEntity<Void> deleteKey(
            @PathVariable UUID accountId,
            @PathVariable UUID keyId) {
        transferKeyService.delete(keyId, accountId);
        return ResponseEntity.noContent().build();
    }
}