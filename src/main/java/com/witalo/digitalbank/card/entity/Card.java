package com.witalo.digitalbank.card.entity;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.card.enums.CardStatus;
import com.witalo.digitalbank.card.enums.CardType;
import com.witalo.digitalbank.card.exception.InvalidCreditLimitException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade que representa um cartão bancário (débito ou crédito).
 * Contém informações como número, CVV, data de expiração, tipo e limite.
 *
 * @author BankDash Team
 */
@Entity
@Table(name = "cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit;

    public Card(Account account, String cardNumber, String cvv, LocalDate expiryDate, CardType type, BigDecimal creditLimit) {
        this.account = account;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.type = type;
        this.creditLimit = type == CardType.CREDIT ? creditLimit : BigDecimal.ZERO;

        if (type == CardType.CREDIT) {
            validateCreditLimit();
        }
    }

    /**
     * Bloqueia o cartão (status = BLOCKED)
     */
    public void block() {
        this.status = CardStatus.BLOCKED;
    }

    /**
     * Remove o cartão (soft delete, status = DELETED)
     */
    public void delete() {
        this.status = CardStatus.DELETED;
    }

    private void validateCreditLimit() {
        if (this.creditLimit == null) {
            throw new InvalidCreditLimitException("Credit limit cannot be null");
        }

        if (this.creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidCreditLimitException("Credit limit must be greater than zero. Current value: " + this.creditLimit);
        }
    }
}