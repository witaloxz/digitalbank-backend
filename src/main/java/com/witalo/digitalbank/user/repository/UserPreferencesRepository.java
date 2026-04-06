package com.witalo.digitalbank.user.repository;

import com.witalo.digitalbank.user.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência das preferências do usuário.
 *
 * @author BankDash Team
 */
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {

    /**
     * Busca preferências pelo ID do usuário
     * @param userId ID do usuário
     * @return Optional contendo as preferências se encontradas
     */
    Optional<UserPreferences> findByUserId(UUID userId);

}