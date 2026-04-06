package com.witalo.digitalbank.account.entity;

import com.witalo.digitalbank.account.enums.TransferKeyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entidade que representa uma chave de transferência (Pix).
 * Suporta chaves do tipo EMAIL, PHONE e CPF.
 *
 * @author BankDash Team
 */
@Entity
@Table(name = "transfer_keys",
        uniqueConstraints = @UniqueConstraint(columnNames = {"type", "value"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransferKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferKeyType type;

    @Column(nullable = false, unique = true)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public TransferKey(TransferKeyType type, String value, Account account) {
        this.type = type;
        this.value = value;
        this.account = account;
    }
}