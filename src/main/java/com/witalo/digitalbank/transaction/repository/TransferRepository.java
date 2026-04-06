package com.witalo.digitalbank.transaction.repository;

import com.witalo.digitalbank.transaction.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência de transferências.
 *
 * @author BankDash Team
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    /**
     * Busca transferência pela chave de idempotência (evita duplicação)
     * @param idempotencyKey chave única da requisição
     * @return Optional contendo a transferência se encontrada
     */
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    /**
     * Busca as 5 transferências mais recentes de uma conta
     * @param fromAccountId ID da conta de origem
     * @return lista das últimas transferências
     */
    List<Transfer> findTop5ByFromAccountIdOrderByCreatedAtDesc(UUID fromAccountId);

}