package com.witalo.digitalbank.insurance.repository;

import com.witalo.digitalbank.insurance.entity.Insurance;
import com.witalo.digitalbank.insurance.enums.InsuranceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository para operações de persistência de solicitações de seguro.
 *
 * @author BankDash Team
 */
@Repository
public interface InsuranceRequestRepository extends JpaRepository<Insurance, UUID> {

    /**
     * Busca solicitações de seguro por status com paginação
     * @param status status da solicitação
     * @param pageable paginação
     * @return página de solicitações
     */
    Page<Insurance> findByStatus(InsuranceStatus status, Pageable pageable);

}