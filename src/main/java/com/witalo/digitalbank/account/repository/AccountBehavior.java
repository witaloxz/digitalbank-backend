package com.witalo.digitalbank.account.repository;

import com.witalo.digitalbank.account.entity.Account;

import java.math.BigDecimal;

/**
 * Interface que define o comportamento específico para cada tipo de conta.
 * Implementações devem conter as regras de negócio para saques, depósitos e taxas.
 *
 * @author BankDash Team
 */
public interface AccountBehavior {

    /**
     * Valida se o saque é permitido para o tipo de conta
     * @param account conta a ser debitada
     * @param amount valor do saque
     * @throws com.witalo.digitalbank.account.exception.AccountBusinessException se a operação não for permitida
     */
    void validateWithdraw(Account account, BigDecimal amount);

    /**
     * Valida se o depósito é permitido para o tipo de conta
     * @param account conta a ser creditada
     * @param amount valor do depósito
     */
    void validateDeposit(Account account, BigDecimal amount);

    /**
     * Calcula a taxa aplicável à transação (opcional)
     * @param amount valor da transação
     * @return valor da taxa (padrão zero)
     */
    default BigDecimal calculateFee(BigDecimal amount) {
        return BigDecimal.ZERO;
    }
}