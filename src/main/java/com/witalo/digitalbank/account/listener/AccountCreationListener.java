package com.witalo.digitalbank.account.listener;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.enums.AccountType;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.account.service.AccountNumberGenerator;
import com.witalo.digitalbank.user.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener responsável por criar uma conta bancária automaticamente
 * quando um novo usuário é cadastrado no sistema.
 *
 * @author BankDash Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCreationListener {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    /**
     * Cria uma conta padrão do tipo CHECKING para o usuário recém-criado
     * @param event evento de usuário criado
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserCreated(UserCreatedEvent event) {
        var user = event.getUser();
        log.info("Creating account for user: {}", user.getEmail());

        try {
            String agency = accountNumberGenerator.getDefaultAgency();
            String accountNumber = accountNumberGenerator.generateNextAccountNumber();
            String digit = accountNumberGenerator.calculateDigit(accountNumber);

            Account account = new Account(
                    agency,
                    accountNumber,
                    digit,
                    AccountType.CHECKING,
                    user
            );

            accountRepository.save(account);

            log.info("Account created successfully for {}. Agency: {}, Account: {}-{}",
                    user.getEmail(), agency, accountNumber, digit);

        } catch (Exception e) {
            log.error("Error creating account for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to create account for user", e);
        }
    }
}