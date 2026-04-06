package com.witalo.digitalbank.account.entity;

import com.witalo.digitalbank.account.enums.AccountStatus;
import com.witalo.digitalbank.account.enums.AccountType;
import com.witalo.digitalbank.account.exception.*;
import com.witalo.digitalbank.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa uma conta bancária do usuário.
 * Contém informações como agência, número, saldo, tipo e status.
 *
 * @author BankDash Team
 */
@Entity
@Builder
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"agency", "accountNumber", "accountDigit"}),
                @UniqueConstraint(columnNames = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 4)
    private String agency;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 2)
    private String accountDigit;

    @Column(nullable = false, precision = 15, scale = 2)
    @NonNull
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Account(String agency, String accountNumber, String accountDigit, AccountType type, User user) {
        this.agency = agency;
        this.accountNumber = accountNumber;
        this.accountDigit = accountDigit;
        this.type = type;
        this.user = user;
        this.balance = BigDecimal.ZERO;
        this.status = AccountStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    /**
     * Atualiza o status da conta
     * @param status novo status
     * @throws InvalidAccountStatusException se status for nulo
     * @throws AccountStatusAlreadyException se já estiver no mesmo status
     */
    public void updateStatus(AccountStatus status) {
        if (status == null) {
            throw new InvalidAccountStatusException();
        }
        if (this.status == status) {
            throw new AccountStatusAlreadyException("Account is already " + status.name().toLowerCase());
        }
        this.status = status;
    }

    /**
     * Desativa a conta
     * @throws AccountAlreadyInactiveException se já estiver inativa
     */
    public void deactivate() {
        if (this.status == AccountStatus.INACTIVE) {
            throw new AccountAlreadyInactiveException();
        }
        this.status = AccountStatus.INACTIVE;
    }

    /**
     * Ativa a conta
     * @throws AccountAlreadyActiveException se já estiver ativa
     */
    public void activate() {
        if (this.status == AccountStatus.ACTIVE) {
            throw new AccountAlreadyActiveException();
        }
        this.status = AccountStatus.ACTIVE;
    }

    /**
     * Realiza um depósito na conta
     * @param amount valor a ser depositado
     * @throws InvalidAmountException se o valor for inválido
     */
    public void deposit(BigDecimal amount) {
        validateAmount(amount);
        this.balance = this.balance.add(amount);
    }

    /**
     * Realiza um saque na conta
     * @param amount valor a ser sacado
     * @throws InvalidAmountException se o valor for inválido
     * @throws InsufficientBalanceException se o saldo for insuficiente
     */
    public void withdraw(BigDecimal amount) {
        validateAmount(amount);
        validateInsufficientBalance(amount);
        this.balance = this.balance.subtract(amount);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException(amount);
        }
    }

    private void validateInsufficientBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(balance, amount);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

        Account account = (Account) o;
        return id != null && Objects.equals(id, account.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}