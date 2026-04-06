package com.witalo.digitalbank.account.repository;

import com.witalo.digitalbank.account.entity.TransferKey;
import com.witalo.digitalbank.account.enums.TransferKeyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência de chaves de transferência (Pix).
 *
 * @author BankDash Team
 */
@Repository
public interface TransferKeyRepository extends JpaRepository<TransferKey, UUID> {

    /**
     * Busca chave pelo tipo e valor
     * @param type tipo da chave (EMAIL, PHONE, CPF)
     * @param value valor da chave
     * @return Optional contendo a chave encontrada
     */
    Optional<TransferKey> findByTypeAndValue(TransferKeyType type, String value);

    /**
     * Lista todas as chaves de uma conta
     * @param accountId ID da conta
     * @return lista de chaves
     */
    List<TransferKey> findByAccountId(UUID accountId);

    /**
     * Verifica se já existe uma chave com o tipo e valor informados
     * @param type tipo da chave
     * @param value valor da chave
     * @return true se existir, false caso contrário
     */
    boolean existsByTypeAndValue(TransferKeyType type, String value);

    /**
     * Verifica se a conta já possui uma chave do tipo informado
     * @param accountId ID da conta
     * @param transferKeyType tipo da chave
     * @return true se existir, false caso contrário
     */
    boolean existsByAccountIdAndType(UUID accountId, TransferKeyType transferKeyType);

}