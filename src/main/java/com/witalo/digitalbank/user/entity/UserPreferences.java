package com.witalo.digitalbank.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entidade que armazena as preferências de configuração do usuário.
 * Inclui idioma, notificações e segurança.
 *
 * @author BankDash Team
 */
@Entity
@Table(name = "user_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 10)
    private String language = "pt-br";

    @Column(nullable = false)
    private boolean emailNotifications = true;

    @Column(nullable = false)
    private boolean smsNotifications = true;

    @Column(nullable = false)
    private boolean pushNotifications = false;

    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    public UserPreferences(User user) {
        this.user = user;
    }

    /**
     * Atualiza o idioma do usuário
     * @param language código do idioma (ex: pt-br, en)
     */
    public void updateLanguage(String language) {
        this.language = language;
    }

    /**
     * Ativa/desativa notificações por e-mail
     * @param enabled status da notificação
     */
    public void updateEmailNotifications(boolean enabled) {
        this.emailNotifications = enabled;
    }

    /**
     * Ativa/desativa notificações por SMS
     * @param enabled status da notificação
     */
    public void updateSmsNotifications(boolean enabled) {
        this.smsNotifications = enabled;
    }

    /**
     * Ativa/desativa notificações push
     * @param enabled status da notificação
     */
    public void updatePushNotifications(boolean enabled) {
        this.pushNotifications = enabled;
    }

    /**
     * Ativa/desativa autenticação de dois fatores
     * @param enabled status do 2FA
     */
    public void updateTwoFactorEnabled(boolean enabled) {
        this.twoFactorEnabled = enabled;
    }
}