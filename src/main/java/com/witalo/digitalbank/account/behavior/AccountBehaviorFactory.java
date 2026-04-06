package com.witalo.digitalbank.account.behavior;

import com.witalo.digitalbank.account.behavior.impl.*;
import com.witalo.digitalbank.account.enums.AccountType;
import com.witalo.digitalbank.account.repository.AccountBehavior;
import org.springframework.stereotype.Component;

/**
 * Fábrica responsável por fornecer o comportamento específico para cada tipo de conta.
 *
 * @author BankDash Team
 */
@Component
public class AccountBehaviorFactory {

    /**
     * Retorna a implementação de comportamento adequada para o tipo de conta
     * @param type tipo da conta (CHECKING, SAVINGS, UNIVERSITY, SALARY)
     * @return instância do comportamento da conta
     * @throws IllegalArgumentException se o tipo de conta não for suportado
     */
    public AccountBehavior getBehavior(AccountType type) {
        return switch (type) {
            case CHECKING -> new CheckingBehavior();
            case SAVINGS -> new SavingsBehavior();
            case UNIVERSITY -> new UniversityBehavior();
            case SALARY -> new SalaryBehavior();
            default -> throw new IllegalArgumentException("Unsupported account type: " + type);
        };
    }
}