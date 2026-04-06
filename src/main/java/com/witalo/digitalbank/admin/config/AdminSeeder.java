package com.witalo.digitalbank.admin.config;

import com.witalo.digitalbank.common.security.EncryptionService;
import com.witalo.digitalbank.system.entity.SystemSettings;
import com.witalo.digitalbank.system.repository.SystemSettingsRepository;
import com.witalo.digitalbank.user.entity.User;
import com.witalo.digitalbank.user.entity.UserPreferences;
import com.witalo.digitalbank.user.enums.UserRole;
import com.witalo.digitalbank.user.repository.UserPreferencesRepository;
import com.witalo.digitalbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Componente responsável por inicializar dados padrão do sistema na primeira execução.
 * Cria administrador padrão e configurações iniciais do sistema.
 *
 * @author BankDash Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final SystemSettingsRepository settingsRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.cpf}")
    private String adminCpf;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Override
    @Transactional
    public void run(String... args) {
        // Cria configuração padrão de modo de manutenção
        if (settingsRepository.findById("maintenanceMode").isEmpty()) {
            settingsRepository.save(new SystemSettings("maintenanceMode", "false"));
            log.info("Maintenance mode setting created with default value: false");
        }

        // Cria administrador padrão se não existir
        if (userRepository.findByRole(UserRole.ADMIN).isEmpty()) {
            log.info("Creating default admin user...");

            String encryptedCpf = encryptionService.encrypt(adminCpf);

            User admin = new User(
                    adminName,
                    LocalDate.of(1970, 1, 1),
                    adminEmail,
                    adminPhone,
                    encryptedCpf,
                    passwordEncoder.encode(adminPassword)
            );
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);

            UserPreferences prefs = new UserPreferences(admin);
            preferencesRepository.save(prefs);

            log.info("Admin created successfully! Email: {}, Password: {}", adminEmail, adminPassword);
        } else {
            log.debug("Admin already exists. No action needed.");
        }
    }
}