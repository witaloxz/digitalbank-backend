package com.witalo.digitalbank.account.repository;

import com.witalo.digitalbank.account.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência de contas bancárias.
 *
 * @author BankDash Team
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Busca conta com lock pessimista para escrita (evita race conditions)
     * @param id ID da conta
     * @return Optional contendo a conta com lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(UUID id);

    /**
     * Busca conta pelo número da conta
     * @param accountNumber número da conta
     * @return Optional contendo a conta encontrada
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Busca conta pelo ID do usuário
     * @param userId ID do usuário
     * @return Optional contendo a conta encontrada
     */
    Optional<Account> findByUserId(UUID userId);

    /**
     * Busca a conta com maior número em uma agência (para gerar próximo número)
     * @param agency código da agência
     * @return Optional contendo a conta com maior número
     */
    Optional<Account> findTopByAgencyOrderByAccountNumberDesc(String agency);

}