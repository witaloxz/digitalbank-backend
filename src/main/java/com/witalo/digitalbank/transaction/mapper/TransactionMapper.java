package com.witalo.digitalbank.transaction.mapper;

import com.witalo.digitalbank.transaction.dto.TransactionResponseDTO;
import com.witalo.digitalbank.transaction.entity.Transaction;
import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;

/**
 * Mapper responsável por converter entidades Transaction em DTOs de resposta.
 *
 * @author BankDash Team
 */
public class TransactionMapper {

    private TransactionMapper() {
        // Construtor privado para evitar instanciação
    }

    /**
     * Converte entidade Transaction para TransactionResponseDTO
     * @param transaction entidade da transação
     * @return DTO com os dados da transação
     */
    public static TransactionResponseDTO toResponseDTO(Transaction transaction) {
        // Define se a transação pode ser revertida
        // Regra: apenas DEPOSIT bem-sucedidos com transferId associado podem ser revertidos
        boolean canReverse = false;
        if (transaction.getType() == TransactionType.DEPOSIT
                && transaction.getTransferId() != null
                && transaction.getStatus() == TransactionStatus.SUCCESS) {
            canReverse = true;
        }

        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getTransferId(),
                transaction.getCreatedAt(),
                canReverse
        );
    }
}