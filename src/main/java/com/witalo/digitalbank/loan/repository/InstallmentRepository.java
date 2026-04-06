package com.witalo.digitalbank.loan.repository;

import com.witalo.digitalbank.loan.entity.Installment;
import com.witalo.digitalbank.loan.enums.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência de parcelas de empréstimo.
 *
 * @author BankDash Team
 */
@Repository
public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    /**
     * Busca parcelas de um empréstimo ordenadas por número
     * @param loanId ID do empréstimo
     * @return lista de parcelas ordenadas
     */
    List<Installment> findByLoanIdOrderByInstallmentNumberAsc(UUID loanId);

    /**
     * Busca parcela pelo código do boleto
     * @param boletoCode código do boleto
     * @return Optional com a parcela encontrada
     */
    Optional<Installment> findByBoletoCode(String boletoCode);

    /**
     * Conta parcelas de um empréstimo com status específico
     * @param loanId ID do empréstimo
     * @param status status da parcela
     * @return quantidade de parcelas
     */
    long countByLoanIdAndStatus(UUID loanId, InstallmentStatus status);

    /**
     * Verifica se existe alguma parcela para o empréstimo
     * @param id ID do empréstimo
     * @return true se existir parcelas, false caso contrário
     */
    boolean existsByLoanId(UUID id);

}