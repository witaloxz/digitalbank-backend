package com.witalo.digitalbank.loan.repository;

import com.witalo.digitalbank.loan.entity.Loan;
import com.witalo.digitalbank.loan.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Repository para operações de persistência de empréstimos.
 *
 * @author BankDash Team
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID>, JpaSpecificationExecutor<Loan> {

    /**
     * Busca empréstimos de uma conta com paginação
     * @param accountId ID da conta
     * @param pageable paginação
     * @return página de empréstimos
     */
    Page<Loan> findByAccountId(UUID accountId, Pageable pageable);

    /**
     * Busca empréstimos por status com paginação
     * @param status status do empréstimo
     * @param pageable paginação
     * @return página de empréstimos
     */
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    /**
     * Soma o valor restante de todos os empréstimos ativos de uma conta
     * @param accountId ID da conta
     * @return soma dos valores restantes
     */
    @Query("SELECT COALESCE(SUM(l.remainingAmount), 0) FROM Loan l WHERE l.account.id = :accountId AND l.status = 'ACTIVE'")
    BigDecimal sumRemainingAmountByAccountId(@Param("accountId") UUID accountId);

    /**
     * Soma o valor das parcelas mensais de todos os empréstimos ativos de uma conta
     * @param accountId ID da conta
     * @return soma das parcelas mensais
     */
    @Query("SELECT COALESCE(SUM(l.monthlyPayment), 0) FROM Loan l WHERE l.account.id = :accountId AND l.status = 'ACTIVE'")
    BigDecimal sumMonthlyPaymentByAccountId(@Param("accountId") UUID accountId);

    /**
     * Calcula a taxa de juros média dos empréstimos ativos de uma conta
     * @param accountId ID da conta
     * @return média das taxas de juros
     */
    @Query("SELECT COALESCE(AVG(l.interestRate), 0) FROM Loan l WHERE l.account.id = :accountId AND l.status = 'ACTIVE'")
    BigDecimal avgInterestRateByAccountId(@Param("accountId") UUID accountId);

    /**
     * Conta a quantidade de empréstimos pendentes de aprovação
     * @return quantidade de empréstimos pendentes
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'PENDING'")
    long countByStatusPending();

}