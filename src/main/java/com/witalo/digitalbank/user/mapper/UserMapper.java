package com.witalo.digitalbank.user.mapper;

import com.witalo.digitalbank.common.security.EncryptionService;
import com.witalo.digitalbank.user.dto.UserPreferencesDTO;
import com.witalo.digitalbank.user.dto.UserResponseDTO;
import com.witalo.digitalbank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper responsável por converter entidades User em DTOs de resposta.
 * Aplica descriptografia do CPF durante a conversão.
 *
 * @author BankDash Team
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final EncryptionService encryptionService;

    /**
     * Converte entidade User para UserResponseDTO
     * @param user entidade do usuário
     * @return DTO com os dados do usuário (CPF descriptografado)
     */
    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) return null;

        String decryptedCpf = encryptionService.decrypt(user.getCpf());

        UserPreferencesDTO preferencesDTO = null;
        if (user.getPreferences() != null) {
            var prefs = user.getPreferences();
            preferencesDTO = new UserPreferencesDTO(
                    prefs.getLanguage(),
                    prefs.isEmailNotifications(),
                    prefs.isSmsNotifications(),
                    prefs.isPushNotifications(),
                    prefs.isTwoFactorEnabled()
            );
        }

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getDateOfBirth(),
                user.getEmail(),
                user.getPhone(),
                decryptedCpf,
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRole(),
                preferencesDTO
        );
    }
}