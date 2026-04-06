package com.witalo.digitalbank.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que armazena configurações globais do sistema.
 * Utiliza chave-valor para armazenar parâmetros como taxas, limites e modo de manutenção.
 *
 * @author BankDash Team
 */
@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {

    @Id
    private String key;

    @Column(nullable = false)
    private String value;

    private LocalDateTime updatedAt;

    public SystemSettings(String key, String value) {
        this.key = key;
        this.value = value;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza o valor da configuração
     * @param value novo valor
     */
    public void updateValue(String value) {
        this.value = value;
        this.updatedAt = LocalDateTime.now();
    }
}