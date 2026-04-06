package com.witalo.digitalbank.user.service;

import com.witalo.digitalbank.user.enums.UserStatus;
import com.witalo.digitalbank.common.exception.BusinessException;
import com.witalo.digitalbank.common.security.EncryptionService;
import com.witalo.digitalbank.user.dto.*;
import com.witalo.digitalbank.user.entity.User;
import com.witalo.digitalbank.user.entity.UserPreferences;
import com.witalo.digitalbank.user.event.UserCreatedEvent;
import com.witalo.digitalbank.user.exception.*;
import com.witalo.digitalbank.user.mapper.UserMapper;
import com.witalo.digitalbank.user.repository.UserPreferencesRepository;
import com.witalo.digitalbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável por todas as operações relacionadas a usuários.
 * Gerencia criação, atualização, consulta e exclusão de usuários.
 *
 * @author BankDash Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserMapper userMapper;

    /**
     * Cria um novo usuário no sistema
     * @param dto dados de criação do usuário
     * @return DTO com os dados do usuário criado
     * @throws BusinessException se houver violação de unicidade (email/CPF duplicado)
     */
    @Transactional
    public UserResponseDTO create(CreateUserRequestDTO dto) {
        validateNotNull(dto, "User data");

        try {
            String encryptedPassword = passwordEncoder.encode(dto.password());
            String encryptedCpf = encryptionService.encrypt(dto.cpf());

            User user = new User(
                    dto.name(),
                    dto.dateOfBirth(),
                    dto.email(),
                    dto.phone(),
                    encryptedCpf,
                    encryptedPassword
            );
            User savedUser = userRepository.save(user);

            // Dispara evento para criação da conta bancária
            eventPublisher.publishEvent(new UserCreatedEvent(savedUser));

            return userMapper.toResponseDTO(savedUser);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation on create: {}", ex.getMessage());
            throw handleDataIntegrityViolation(ex, dto);
        }
    }

    /**
     * Busca todos os usuários com paginação
     * @param pageable informações de paginação
     * @return página de DTOs de usuários
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllPaged(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDTO);
    }

    /**
     * Busca usuário por ID
     * @param id identificador do usuário
     * @return DTO do usuário encontrado
     * @throws UserNotFoundException se o usuário não existir
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userMapper.toResponseDTO(findUserById(id));
    }

    /**
     * Busca usuário por e-mail
     * @param email e-mail do usuário
     * @return DTO do usuário encontrado
     * @throws EmailNotFoundException se o e-mail não existir
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findByEmail(String email) {
        validateNotBlank(email, "Email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(email));
        return userMapper.toResponseDTO(user);
    }

    /**
     * Busca usuário por CPF
     * @param cpf CPF do usuário
     * @return DTO do usuário encontrado
     * @throws CpfNotFoundException se o CPF não existir
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findByCpf(String cpf) {
        validateNotBlank(cpf, "CPF");
        String encryptedCpf = encryptionService.encrypt(cpf);
        User user = userRepository.findByCpf(encryptedCpf)
                .orElseThrow(() -> new CpfNotFoundException(cpf));
        return userMapper.toResponseDTO(user);
    }

    /**
     * Atualiza dados do usuário (campos opcionais)
     * @param id identificador do usuário
     * @param dto dados para atualização
     * @return DTO com os dados atualizados
     * @throws BusinessException se nenhum campo for fornecido para atualização
     */
    @Transactional
    public UserResponseDTO update(UUID id, UpdateUserRequestDTO dto) {
        validateNotNull(dto, "Update data");
        User user = findUserById(id);

        boolean changed = false;

        if (dto.name() != null && !dto.name().isBlank()) {
            user.updateName(dto.name());
            changed = true;
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            userRepository.findByEmail(dto.email())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(user.getId())) {
                            throw new EmailAlreadyExistsException(dto.email());
                        }
                    });
            user.updateEmail(dto.email());
            changed = true;
        }

        if (dto.dateOfBirth() != null) {
            user.updateDateOfBirth(dto.dateOfBirth());
            changed = true;
        }

        if (dto.phone() != null && !dto.phone().isBlank()) {
            user.updatePhone(dto.phone());
            changed = true;
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            String encrypted = passwordEncoder.encode(dto.password());
            user.updatePassword(encrypted);
            changed = true;
        }

        if (!changed) {
            throw new BusinessException("No fields to update were provided");
        }

        return userMapper.toResponseDTO(user);
    }

    /**
     * Atualiza a senha do usuário
     * @param id identificador do usuário
     * @param dto dados com senha atual, nova e confirmação
     * @throws BusinessException se a senha atual estiver incorreta ou as senhas não coincidirem
     */
    @Transactional
    public void updatePassword(UUID id, UpdatePasswordRequestDTO dto) {
        User user = findUserById(id);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        if (!dto.newPassword().equals(dto.confirmNewPassword())) {
            throw new BusinessException("New password and confirmation do not match");
        }

        String encryptedNew = passwordEncoder.encode(dto.newPassword());
        user.updatePassword(encryptedNew);
    }

    /**
     * Atualiza as preferências do usuário
     * @param userId identificador do usuário
     * @param dto dados das preferências
     */
    @Transactional
    public void updatePreferences(UUID userId, UserPreferencesDTO dto) {
        User user = findUserById(userId);

        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreferences newPrefs = new UserPreferences(user);
                    preferencesRepository.save(newPrefs);
                    return newPrefs;
                });

        if (dto.language() != null && !dto.language().isBlank()) {
            preferences.updateLanguage(dto.language());
        }
        preferences.updateEmailNotifications(dto.emailNotifications());
        preferences.updateSmsNotifications(dto.smsNotifications());
        preferences.updatePushNotifications(dto.pushNotifications());
        preferences.updateTwoFactorEnabled(dto.twoFactorEnabled());
    }

    /**
     * Alterna o status do usuário entre ATIVO e INATIVO
     * @param userId identificador do usuário
     */
    @Transactional
    public void toggleUserStatus(UUID userId) {
        User user = findUserById(userId);
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.deactivate();
        } else {
            user.activate();
        }
    }

    /**
     * Desativa um usuário (soft delete)
     * @param id identificador do usuário
     */
    @Transactional
    public void delete(UUID id) {
        User user = findUserById(id);
        user.deactivate();
    }

    /**
     * Busca entidade User por ID
     * @param id identificador do usuário
     * @return entidade User
     * @throws UserNotFoundException se o usuário não existir
     */
    private User findUserById(UUID id) {
        validateNotNull(id, "ID");
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Trata violações de unicidade (email ou CPF duplicado)
     */
    private RuntimeException handleDataIntegrityViolation(DataIntegrityViolationException ex, CreateUserRequestDTO dto) {
        String message = ex.getMostSpecificCause().getMessage();
        if (message.contains("users_email_key") || message.contains("UK_email")) {
            return new EmailAlreadyExistsException(dto.email());
        }
        if (message.contains("users_cpf_key") || message.contains("UK_cpf")) {
            return new CpfAlreadyExistsException(dto.cpf());
        }
        log.error("Unexpected data integrity violation", ex);
        return new BusinessException("Data integrity error. Please check unique fields.");
    }

    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(fieldName + " is required");
        }
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(fieldName + " is required");
        }
    }
}