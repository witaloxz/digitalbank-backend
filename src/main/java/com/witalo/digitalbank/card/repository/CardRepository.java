package com.witalo.digitalbank.card.repository;

import com.witalo.digitalbank.card.entity.Card;
import com.witalo.digitalbank.card.enums.CardStatus;
import com.witalo.digitalbank.card.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository para operações de persistência de cartões.
 *
 * @author BankDash Team
 */
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    /**
     * Busca cartões de uma conta que não estão deletados
     * @param accountId ID da conta
     * @param status status a ser excluído (DELETED)
     * @return lista de cartões ativos
     */
    List<Card> findByAccountIdAndStatusNot(UUID accountId, CardStatus status);

    /**
     * Conta quantidade de cartões ativos de uma conta
     * @param accountId ID da conta
     * @param status status a ser excluído (DELETED)
     * @return quantidade de cartões ativos
     */
    long countByAccountIdAndStatusNot(UUID accountId, CardStatus status);

    /**
     * Verifica se já existe um cartão com o número informado
     * @param cardNumber número do cartão
     * @return true se existir, false caso contrário
     */
    boolean existsByCardNumber(String cardNumber);

    /**
     * Verifica se a conta já possui um cartão ativo do tipo informado
     * @param accountId ID da conta
     * @param type tipo do cartão (DEBIT/CREDIT)
     * @param status status a ser excluído (DELETED)
     * @return true se existir, false caso contrário
     */
    boolean existsByAccountIdAndTypeAndStatusNot(UUID accountId, CardType type, CardStatus status);

}