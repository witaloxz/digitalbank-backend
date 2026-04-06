package com.witalo.digitalbank.insurance.controller;

import com.witalo.digitalbank.insurance.dto.AdminInsuranceDTO;
import com.witalo.digitalbank.insurance.dto.InsuranceRequestDTO;
import com.witalo.digitalbank.insurance.dto.InsuranceResponseDTO;
import com.witalo.digitalbank.insurance.service.InsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller responsável pelos endpoints de seguro de vida.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/insurance")
@RequiredArgsConstructor
@Tag(name = "Insurance", description = "Insurance services endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InsuranceController {

    private final InsuranceService insuranceService;

    /**
     * Solicita um seguro de vida
     * @param accountId ID da conta
     * @param dto dados da solicitação (plano)
     * @return solicitação criada
     */
    @PostMapping("/life/request")
    @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #accountId, 'account', 'write')")
    @Operation(summary = "Request life insurance", description = "Submits a life insurance request for approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Insurance request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid plan or data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<InsuranceResponseDTO> requestLifeInsurance(
            @RequestParam UUID accountId,
            @Valid @RequestBody InsuranceRequestDTO dto) {
        return ResponseEntity.ok(insuranceService.requestLifeInsurance(accountId, dto));
    }

    /**
     * Lista solicitações de seguro pendentes (Admin)
     * @param pageable paginação
     * @return página de solicitações pendentes
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending insurance requests", description = "Returns all pending insurance requests. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending requests returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (requires ADMIN role)")
    })
    public ResponseEntity<Page<AdminInsuranceDTO>> getPendingRequests(Pageable pageable) {
        return ResponseEntity.ok(insuranceService.getPendingRequests(pageable));
    }

    /**
     * Aprova uma solicitação de seguro (Admin)
     * @param id ID da solicitação
     */
    @PatchMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve insurance request", description = "Approves a pending insurance request. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insurance request approved successfully"),
            @ApiResponse(responseCode = "400", description = "Request cannot be approved"),
            @ApiResponse(responseCode = "403", description = "Access denied (requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Insurance request not found")
    })
    public ResponseEntity<Void> approveRequest(@PathVariable UUID id) {
        insuranceService.approveRequest(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rejeita uma solicitação de seguro (Admin)
     * @param id ID da solicitação
     */
    @PatchMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject insurance request", description = "Rejects a pending insurance request. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insurance request rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Request cannot be rejected"),
            @ApiResponse(responseCode = "403", description = "Access denied (requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Insurance request not found")
    })
    public ResponseEntity<Void> rejectRequest(@PathVariable UUID id) {
        insuranceService.rejectRequest(id);
        return ResponseEntity.noContent().build();
    }
}