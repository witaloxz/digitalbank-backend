package com.witalo.digitalbank.insurance.entity;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.common.exception.BusinessException;
import com.witalo.digitalbank.insurance.enums.InsurancePlan;
import com.witalo.digitalbank.insurance.enums.InsuranceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa uma solicitação de seguro de vida.
 *
 * @author BankDash Team
 */
@Entity
@Table(name = "insurance_requests")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    private InsurancePlan plan;

    @Enumerated(EnumType.STRING)
    private InsuranceStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = InsuranceStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Aprova a solicitação de seguro
     * @throws BusinessException se a solicitação não estiver pendente
     */
    public void approve() {
        if (this.status != InsuranceStatus.PENDING) {
            throw new BusinessException("Only pending requests can be approved");
        }
        this.status = InsuranceStatus.APPROVED;
    }

    /**
     * Rejeita a solicitação de seguro
     * @throws BusinessException se a solicitação não estiver pendente
     */
    public void reject() {
        if (this.status != InsuranceStatus.PENDING) {
            throw new BusinessException("Only pending requests can be rejected");
        }
        this.status = InsuranceStatus.REJECTED;
    }
}