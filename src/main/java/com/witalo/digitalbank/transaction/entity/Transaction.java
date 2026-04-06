package com.witalo.digitalbank.transaction.entity;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;
import com.witalo.digitalbank.common.exception.BusinessException;
import com.witalo.digitalbank.transaction.exception.TransactionBusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa uma transação financeira no sistema.
 * Registra depósitos, saques e transferências, mantendo histórico de saldo.
 *
 * @author BankDash Team
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "transfer_id")
    private UUID transferId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction(Account account, TransactionType type, BigDecimal amount,
                       String description, UUID transferId, TransactionStatus status, BigDecimal balanceAfter) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transferId = transferId;
        this.status = status;
        this.balanceAfter = balanceAfter;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Reverte a transação, estornando o valor na conta
     * @throws BusinessException se a transação não puder ser revertida
     */
    public void reverse() {
        if (this.status != TransactionStatus.SUCCESS) {
            throw new BusinessException("Only SUCCESS transactions can be reversed");
        }
        switch (type) {
            case DEPOSIT -> account.withdraw(amount);
            case WITHDRAW, TRANSFER -> account.deposit(amount);
            default -> throw new TransactionBusinessException("Transaction type not supported for reversal: " + type);
        }
        this.status = TransactionStatus.REVERSED;
        this.balanceAfter = account.getBalance();
    }

    public void setStatus(TransactionStatus newStatus) {
        this.status = newStatus;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Transaction that = (Transaction) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}