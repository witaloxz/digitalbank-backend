package com.witalo.digitalbank.admin.controller;

import com.witalo.digitalbank.system.service.SystemSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsável pelos endpoints de configurações do sistema (admin).
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Settings", description = "Administrative system settings endpoints")
public class AdminSettingsController {

    private final SystemSettingsService settingsService;

    /**
     * Retorna todas as configurações do sistema
     * @return mapa com as configurações (chave-valor)
     */
    @GetMapping
    @Operation(summary = "Get all system settings", description = "Returns all system configuration parameters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settings returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Map<String, String>> getSettings() {
        return ResponseEntity.ok(settingsService.getAllSettings());
    }

    /**
     * Atualiza as configurações do sistema
     * @param settings mapa com as novas configurações
     */
    @PutMapping
    @Operation(summary = "Update system settings", description = "Updates system configuration parameters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Settings updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> updateSettings(@RequestBody Map<String, String> settings) {
        settingsService.updateSettings(settings);
        return ResponseEntity.noContent().build();
    }
}