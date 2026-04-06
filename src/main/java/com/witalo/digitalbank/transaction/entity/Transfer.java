package com.witalo.digitalbank.transaction.entity;

import com.witalo.digitalbank.transaction.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa uma transferência entre contas.
 * Possui controle de idempotência via chave única e rastreamento de status.
 *
 * @author BankDash Team
 */
@Entity
@Table(name = "transfers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID fromAccountId;

    @Column(nullable = false)
    private UUID toAccountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private LocalDateTime createdAt;

    public Transfer(String idempotencyKey, UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        this.idempotencyKey = idempotencyKey;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = TransferStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Marca a transferência como concluída com sucesso
     */
    public void markAsCompleted() {
        this.status = TransferStatus.COMPLETED;
    }

    /**
     * Marca a transferência como falha
     */
    public void markAsFailed() {
        this.status = TransferStatus.FAILED;
    }

    /**
     * Marca a transferência como revertida (estorno)
     */
    public void markAsReversed() {
        this.status = TransferStatus.REVERSED;
    }
}