package com.witalo.digitalbank.loan.entity;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.loan.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um empréstimo solicitado por um usuário.
 * Contém informações de valor, taxa, parcelas e status.
 *
 * @author BankDash Team
 */
@Entity
@Table(name = "loans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable =false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private Integer progressPercentage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = LoanStatus.ACTIVE;
        }
        if (progressPercentage == null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = remainingAmount.multiply(BigDecimal.valueOf(100))
                    .divide(totalAmount, 0, RoundingMode.DOWN)
                    .intValue();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calcula e atualiza o percentual de progresso do empréstimo
     */
    public void updateProgressPercentage() {
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.progressPercentage = remainingAmount.multiply(BigDecimal.valueOf(100))
                    .divide(totalAmount, 0, RoundingMode.DOWN)
                    .intValue();
        }
    }
}