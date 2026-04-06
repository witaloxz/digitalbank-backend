package com.witalo.digitalbank.account.behavior.impl;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.InsufficientBalanceException;
import com.witalo.digitalbank.account.repository.AccountBehavior;

import java.math.BigDecimal;

/**
 * Comportamento para conta poupança (SAVINGS).
 * Permite saque apenas dentro do saldo disponível.
 * Possui limite de 2 saques gratuitos por mês (controle deve ser implementado separadamente).
 *
 * @author BankDash Team
 */
public class SavingsBehavior implements AccountBehavior {

    private static final int FREE_WITHDRAWALS_PER_MONTH = 2;

    @Override
    public void validateWithdraw(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }

        // Exemplo: verificar número de saques no mês
        // Seria necessário um contador no campo da Account ou em entidade separada.
        // int withdraws = account.getWithdrawCountThisMonth();
        // if (withdraws >= FREE_WITHDRAWALS_PER_MONTH) {
        //     throw new BusinessException("Free withdrawal limit exceeded");
        // }
    }

    @Override
    public void validateDeposit(Account account, BigDecimal amount) {
        // Depósitos sempre permitidos
    }
}