package com.witalo.digitalbank.transaction.repository;

import com.witalo.digitalbank.transaction.dto.AdminTransactionResponseDTO;
import com.witalo.digitalbank.transaction.entity.Transaction;
import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository para operações de persistência de transações.
 *
 * @author BankDash Team
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    /**
     * Busca transações de uma conta ordenadas por data decrescente
     * @param accountId ID da conta
     * @return lista de transações
     */
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    /**
     * Busca transações por ID de transferência associada
     * @param transferId ID da transferência
     * @return lista de transações
     */
    List<Transaction> findByTransferId(UUID transferId);

    /**
     * Soma total de depósitos bem-sucedidos
     * @return valor total dos depósitos
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'DEPOSIT' AND t.status = 'SUCCESS'")
    BigDecimal sumAmountByTypeDepositAndStatusSuccess();

    /**
     * Busca transações para o painel administrativo com filtros
     * @param search termo de busca (nome do usuário)
     * @param type tipo da transação
     * @param status status da transação
     * @param pageable paginação
     * @return página de DTOs administrativos
     */
    @Query("SELECT new com.witalo.digitalbank.transaction.dto.AdminTransactionResponseDTO(" +
            "t.id, " +
            "CASE WHEN t.type = 'DEPOSIT' THEN 'External' " +
            "     WHEN t.type = 'WITHDRAW' THEN u.name ELSE u.name END, " +
            "CASE WHEN t.type = 'DEPOSIT' THEN u.name " +
            "     WHEN t.type = 'WITHDRAW' THEN 'ATM' " +
            "     ELSE (SELECT u2.name FROM User u2 WHERE u2.id = (SELECT a.user.id FROM Account a WHERE a.id = tr.toAccountId)) END, " +
            "t.amount, " +
            "CASE WHEN t.type = 'DEPOSIT' THEN 'deposit' " +
            "     WHEN t.type = 'WITHDRAW' THEN 'withdrawal' ELSE 'transfer' END, " +
            "CASE WHEN t.status = 'SUCCESS' THEN 'completed' " +
            "     WHEN t.status = 'PENDING' THEN 'pending' ELSE 'failed' END, " +
            "t.createdAt) " +
            "FROM Transaction t " +
            "JOIN t.account a " +
            "JOIN a.user u " +
            "LEFT JOIN Transfer tr ON t.transferId = tr.id " +
            "WHERE (:search IS NULL OR :search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:status IS NULL OR t.status = :status)")
    Page<AdminTransactionResponseDTO> findAdminTransactions(
            @Param("search") String search,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            Pageable pageable
    );

    /**
     * Conta quantidade de transações agrupadas por status
     * @return lista de arrays [status, quantidade]
     */
    @Query("SELECT t.status, COUNT(t) FROM Transaction t GROUP BY t.status")
    List<Object[]> countByStatus();

    /**
     * Soma valores de depósitos por mês
     * @param pageable paginação
     * @return lista de arrays [mês, total]
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', t.createdAt) as month, SUM(t.amount) " +
            "FROM Transaction t WHERE t.type = 'DEPOSIT' AND t.status = 'SUCCESS' " +
            "GROUP BY month ORDER BY month DESC")
    List<Object[]> sumAmountByMonth(Pageable pageable);

    /**
     * Soma receita por mês em um período
     * @param startDate data inicial
     * @param endDate data final
     * @return lista de arrays [mês, total]
     */
    @Query("SELECT FUNCTION('TO_CHAR', t.createdAt, 'Mon') as month, SUM(t.amount) as total " +
            "FROM Transaction t WHERE t.type = 'DEPOSIT' AND t.status = 'SUCCESS' " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY month ORDER BY MIN(t.createdAt)")
    List<Object[]> sumRevenueByMonth(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Conta transações por mês em um período
     * @param startDate data inicial
     * @param endDate data final
     * @return lista de arrays [mês, quantidade]
     */
    @Query("SELECT FUNCTION('TO_CHAR', t.createdAt, 'Mon') as month, COUNT(t) as count " +
            "FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY month ORDER BY MIN(t.createdAt)")
    List<Object[]> countTransactionsByMonth(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

}