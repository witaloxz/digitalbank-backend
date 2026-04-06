package com.witalo.digitalbank.user.controller;

import com.witalo.digitalbank.common.security.UserPrincipal;
import com.witalo.digitalbank.user.dto.*;
import com.witalo.digitalbank.user.service.UserService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller responsável pelos endpoints de gerenciamento de usuários.
 * Fornece operações de CRUD, consultas e gerenciamento de perfil.
 *
 * @author BankDash Team
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    /**
     * Cria um novo usuário no sistema
     * @param dto dados de criação do usuário
     * @return usuário criado com status 201
     */
    @PostMapping
    @Operation(summary = "Create a new user", description = "Registers a new user in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "409", description = "Email or CPF already registered")
    })
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody CreateUserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    /**
     * Retorna o perfil do usuário autenticado
     * @param authentication dados de autenticação
     * @return dados do usuário logado
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userService.findById(principal.getId()));
    }

    /**
     * Lista todos os usuários com paginação (apenas ADMIN)
     * @param pageable parâmetros de paginação
     * @return página de usuários
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users", description = "Returns a page of users. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of users returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (user is not ADMIN)")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<UserResponseDTO>> findAllPaged(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(userService.findAllPaged(pageable));
    }

    /**
     * Busca usuário por ID
     * @param id identificador do usuário
     * @param authentication dados de autenticação
     * @return dados do usuário encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @Operation(summary = "Get user by ID", description = "Returns data of a specific user. Requires being the user themselves or ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Busca usuário por e-mail (apenas ADMIN)
     * @param email e-mail do usuário
     * @return dados do usuário encontrado
     */
    @GetMapping("/by-email")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by email", description = "Returns a user by email. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    /**
     * Busca usuário por CPF (apenas ADMIN)
     * @param cpf CPF do usuário
     * @return dados do usuário encontrado
     */
    @GetMapping("/by-cpf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by CPF", description = "Returns a user by CPF. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> findByCpf(@RequestParam String cpf) {
        return ResponseEntity.ok(userService.findByCpf(cpf));
    }

    /**
     * Atualiza dados de um usuário específico
     * @param id identificador do usuário
     * @param dto dados para atualização
     * @param authentication dados de autenticação
     * @return usuário atualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @Operation(summary = "Update user", description = "Updates user data. Requires being the user themselves or ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    /**
     * Desativa um usuário (soft delete)
     * @param id identificador do usuário
     * @param authentication dados de autenticação
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @Operation(summary = "Deactivate user", description = "Performs soft delete of the user. Requires being the user themselves or ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Atualiza o perfil do usuário autenticado
     * @param dto dados para atualização
     * @param authentication dados de autenticação
     * @return usuário atualizado
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile", description = "Updates the profile of the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequestDTO dto,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userService.update(principal.getId(), dto));
    }

    /**
     * Altera a senha do usuário autenticado
     * @param dto dados com senha atual e nova
     * @param authentication dados de autenticação
     */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change current user password", description = "Changes the password of the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or password mismatch"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> updateCurrentUserPassword(
            @Valid @RequestBody UpdatePasswordRequestDTO dto,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        userService.updatePassword(principal.getId(), dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Atualiza as preferências do usuário autenticado
     * @param preferences dados das preferências
     * @param authentication dados de autenticação
     */
    @PutMapping("/me/preferences")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update user preferences", description = "Updates the preferences of the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Preferences updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> updatePreferences(
            @Valid @RequestBody UserPreferencesDTO preferences,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        userService.updatePreferences(principal.getId(), preferences);
        return ResponseEntity.noContent().build();
    }
}