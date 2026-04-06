package com.witalo.digitalbank.card.controller;

import com.witalo.digitalbank.card.dto.CardResponseDTO;
import com.witalo.digitalbank.card.dto.CreateCardRequestDTO;
import com.witalo.digitalbank.card.service.CardService;
import com.witalo.digitalbank.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsável pelos endpoints de gerenciamento de cartões.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Endpoints for card management")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    /**
     * Cria um novo cartão virtual
     * @param dto dados do cartão (tipo e limite)
     * @param authentication dados de autenticação
     * @return cartão criado
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new virtual card", description = "Creates a new debit or credit card for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or card limit exceeded"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CardResponseDTO> createCard(
            @Valid @RequestBody CreateCardRequestDTO dto,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID accountId = principal.getAccountId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.createCard(accountId, dto));
    }

    /**
     * Lista todos os cartões do usuário autenticado
     * @param authentication dados de autenticação
     * @return lista de cartões
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all cards of the authenticated user", description = "Returns all active cards for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cards returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CardResponseDTO>> getMyCards(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID accountId = principal.getAccountId();
        return ResponseEntity.ok(cardService.getCardsByAccount(accountId));
    }

    /**
     * Bloqueia um cartão
     * @param cardId ID do cartão
     * @param authentication dados de autenticação
     */
    @PatchMapping("/{cardId}/block")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication)")
    @Operation(summary = "Block a card", description = "Blocks the specified card, preventing further transactions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card blocked successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> blockCard(@PathVariable UUID cardId, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        cardService.blockCard(cardId, principal.getAccountId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove um cartão (soft delete)
     * @param cardId ID do cartão
     * @param authentication dados de autenticação
     */
    @DeleteMapping("/{cardId}")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication)")
    @Operation(summary = "Delete (soft delete) a card", description = "Soft deletes the specified card.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        cardService.deleteCard(cardId, principal.getAccountId());
        return ResponseEntity.noContent().build();
    }
}