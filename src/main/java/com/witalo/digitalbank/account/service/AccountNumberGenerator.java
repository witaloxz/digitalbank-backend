package com.witalo.digitalbank.account.service;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serviço responsável pela geração de número de conta e dígito verificador.
 *
 * @author BankDash Team
 */
@Service
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private static final int ACCOUNT_NUMBER_LENGTH = 6;

    private final AccountRepository accountRepository;

    @Value("${app.account.default-agency:0001}")
    private String defaultAgency;

    /**
     * Retorna a agência padrão configurada
     * @return código da agência
     */
    public String getDefaultAgency() {
        return defaultAgency;
    }

    /**
     * Gera o próximo número de conta disponível na agência
     * @return número da conta com 6 dígitos
     */
    public String generateNextAccountNumber() {
        Optional<Account> lastAccount = accountRepository.findTopByAgencyOrderByAccountNumberDesc(defaultAgency);
        int nextNumber = lastAccount
                .map(acc -> Integer.parseInt(acc.getAccountNumber()) + 1)
                .orElse(1);
        return String.format("%0" + ACCOUNT_NUMBER_LENGTH + "d", nextNumber);
    }

    /**
     * Calcula o dígito verificador da conta usando algoritmo módulo 11
     * @param accountNumber número da conta
     * @return dígito verificador
     */
    public String calculateDigit(String accountNumber) {
        String base = defaultAgency + accountNumber;
        int sum = 0;
        int weight = 2;

        for (int i = base.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(base.charAt(i));
            sum += digit * weight;
            weight++;
            if (weight > 9) {
                weight = 2;
            }
        }

        int remainder = sum % 11;
        int digit = 11 - remainder;

        if (digit == 10) {
            digit = 0;
        }
        if (digit == 11) {
            digit = 1;
        }

        return String.valueOf(digit);
    }
}