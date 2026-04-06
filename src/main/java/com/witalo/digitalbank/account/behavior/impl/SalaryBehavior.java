package com.witalo.digitalbank.account.behavior.impl;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.InsufficientBalanceException;
import com.witalo.digitalbank.account.repository.AccountBehavior;

import java.math.BigDecimal;

/**
 * Comportamento para conta salário (SALARY).
 * Permite apenas saque dentro do saldo disponível, sem cheque especial.
 * Depósitos são permitidos apenas pelo empregador (regra de negócio em nível superior).
 *
 * @author BankDash Team
 */
public class SalaryBehavior implements AccountBehavior {

    @Override
    public void validateWithdraw(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }
    }

    @Override
    public void validateDeposit(Account account, BigDecimal amount) {
        // Depósitos são permitidos (apenas o empregador pode depositar - regra de nível superior)
    }
}