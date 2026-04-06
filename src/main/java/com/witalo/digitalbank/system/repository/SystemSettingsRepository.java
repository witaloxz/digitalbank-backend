package com.witalo.digitalbank.system.repository;

import com.witalo.digitalbank.system.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository para operações de persistência das configurações do sistema.
 *
 * @author BankDash Team
 */
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, String> {
}