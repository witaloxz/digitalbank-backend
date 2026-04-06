package com.witalo.digitalbank.account.behavior.impl;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.InsufficientBalanceException;
import com.witalo.digitalbank.account.repository.AccountBehavior;

import java.math.BigDecimal;

/**
 * Comportamento para conta corrente (CHECKING).
 * Permite saque com limite de cheque especial de R$ 500,00.
 *
 * @author BankDash Team
 */
public class CheckingBehavior implements AccountBehavior {

    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("500.00");

    @Override
    public void validateWithdraw(Account account, BigDecimal amount) {
        BigDecimal available = account.getBalance().add(OVERDRAFT_LIMIT);
        if (available.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }
    }

    @Override
    public void validateDeposit(Account account, BigDecimal amount) {
        // Sem restrições para depósito
    }
}