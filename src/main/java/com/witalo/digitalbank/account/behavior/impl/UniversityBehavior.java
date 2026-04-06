package com.witalo.digitalbank.account.behavior.impl;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.InsufficientBalanceException;
import com.witalo.digitalbank.account.repository.AccountBehavior;
import com.witalo.digitalbank.common.exception.BusinessException;

import java.math.BigDecimal;

/**
 * Comportamento para conta universitária (UNIVERSITY).
 * Possui limite máximo por transação de R$ 500,00 para saques e depósitos.
 *
 * @author BankDash Team
 */
public class UniversityBehavior implements AccountBehavior {

    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("500.00");

    @Override
    public void validateWithdraw(Account account, BigDecimal amount) {
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new BusinessException("Withdrawal amount exceeds limit for university account");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }
    }

    @Override
    public void validateDeposit(Account account, BigDecimal amount) {
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new BusinessException("Deposit amount exceeds limit for university account");
        }
    }
}