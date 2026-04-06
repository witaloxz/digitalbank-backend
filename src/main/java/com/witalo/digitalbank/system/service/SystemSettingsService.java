package com.witalo.digitalbank.system.service;

import com.witalo.digitalbank.system.entity.SystemSettings;
import com.witalo.digitalbank.system.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço responsável pelo gerenciamento das configurações globais do sistema.
 * Gerencia taxas, limites e modo de manutenção.
 *
 * @author BankDash Team
 */
@Service
@RequiredArgsConstructor
public class SystemSettingsService {

    private final SystemSettingsRepository settingsRepository;

    /**
     * Retorna todas as configurações do sistema
     * @return mapa com todas as configurações (chave-valor)
     */
    @Transactional(readOnly = true)
    public Map<String, String> getAllSettings() {
        Map<String, String> settings = new HashMap<>();
        settingsRepository.findAll().forEach(s -> settings.put(s.getKey(), s.getValue()));
        return settings;
    }

    /**
     * Atualiza múltiplas configurações do sistema
     * @param newSettings mapa com as novas configurações
     */
    @Transactional
    public void updateSettings(Map<String, String> newSettings) {
        newSettings.forEach((key, value) -> {
            settingsRepository.findById(key).ifPresentOrElse(
                    setting -> setting.updateValue(value),
                    () -> settingsRepository.save(new SystemSettings(key, value))
            );
        });
    }

    /**
     * Verifica se o modo de manutenção está ativo
     * @return true se modo de manutenção estiver ativo, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean isMaintenanceMode() {
        return settingsRepository.findById("maintenanceMode")
                .map(setting -> "true".equalsIgnoreCase(setting.getValue()))
                .orElse(false);
    }
}