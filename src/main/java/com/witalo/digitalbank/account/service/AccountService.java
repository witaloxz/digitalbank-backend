package com.witalo.digitalbank.account.service;

import com.witalo.digitalbank.account.repository.AccountBehavior;
import com.witalo.digitalbank.account.behavior.AccountBehaviorFactory;
import com.witalo.digitalbank.account.dto.*;
import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.*;
import com.witalo.digitalbank.account.mapper.AccountMapper;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.common.exception.BusinessException;
import com.witalo.digitalbank.common.security.UserPrincipal;
import com.witalo.digitalbank.transaction.dto.TransactionResponseDTO;
import com.witalo.digitalbank.transaction.mapper.TransactionMapper;
import com.witalo.digitalbank.transaction.repository.TransactionRepository;
import com.witalo.digitalbank.user.entity.User;
import com.witalo.digitalbank.user.exception.UserNotFoundException;
import com.witalo.digitalbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por todas as operações relacionadas a contas bancárias.
 * Gerencia criação, consulta, atualização, depósitos, saques e extrato.
 *
 * @author BankDash Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountBehaviorFactory behaviorFactory;

    /**
     * Cria uma nova conta bancária para um usuário
     * @param dto dados da conta (tipo e userId)
     * @return DTO da conta criada
     */
    @Transactional
    public AccountResponseDTO create(CreateAccountRequestDTO dto) {
        validateCreateDTO(dto);
        log.info("Creating account for user: {}", dto.userId());

        User user = findUserById(dto.userId());
        validateUserHasNoAccount(user.getId());

        String agency = accountNumberGenerator.getDefaultAgency();
        String accountNumber = accountNumberGenerator.generateNextAccountNumber();
        String digit = accountNumberGenerator.calculateDigit(accountNumber);

        Account account = new Account(agency, accountNumber, digit, dto.type(), user);

        try {
            Account savedAccount = accountRepository.save(account);
            log.info("Account created: agency={}, number={}-{}, ID={}",
                    agency, accountNumber, digit, savedAccount.getId());
            return AccountMapper.toResponseDTO(savedAccount);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation on create: {}", ex.getMessage());
            throw handleDataIntegrityViolation(ex);
        }
    }

    /**
     * Lista todas as contas com paginação
     * @param pageable paginação
     * @return página de contas
     */
    @Transactional(readOnly = true)
    public Page<AccountResponseDTO> findAllPaged(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(AccountMapper::toResponseDTO);
    }

    /**
     * Retorna o extrato da conta
     * @param accountId ID da conta
     * @return DTO com saldo e lista de transações
     */
    @Transactional(readOnly = true)
    public AccountStatementDTO getStatement(UUID accountId) {
        Account account = findAccountById(accountId);
        List<TransactionResponseDTO> transactions = transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(TransactionMapper::toResponseDTO)
                .toList();
        return new AccountStatementDTO(account.getId(), account.getBalance(), transactions);
    }

    /**
     * Busca conta por ID
     * @param id ID da conta
     * @return DTO da conta encontrada
     */
    @Transactional(readOnly = true)
    public AccountResponseDTO findById(UUID id) {
        return AccountMapper.toResponseDTO(findAccountById(id));
    }

    /**
     * Busca conta por ID do usuário
     * @param userId ID do usuário
     * @return DTO da conta encontrada
     */
    @Transactional(readOnly = true)
    public AccountResponseDTO findByUserId(UUID userId) {
        validateId(userId);
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException(userId));
        return AccountMapper.toResponseDTO(account);
    }

    /**
     * Busca conta pelo número da conta
     * @param accountNumber número da conta
     * @return DTO da conta encontrada
     */
    @Transactional(readOnly = true)
    public AccountResponseDTO findByAccountNumber(String accountNumber) {
        validateString(accountNumber, "Account number");
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return AccountMapper.toResponseDTO(account);
    }

    /**
     * Realiza um saque na conta
     * @param accountId ID da conta
     * @param amount valor do saque
     */
    @Transactional
    public void withdraw(UUID accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);

        if (!account.isActive()) {
            throw new AccountInactiveException();
        }

        AccountBehavior behavior = behaviorFactory.getBehavior(account.getType());
        behavior.validateWithdraw(account, amount);
        account.withdraw(amount);
    }

    /**
     * Realiza um depósito na conta
     * @param accountId ID da conta
     * @param amount valor do depósito
     */
    @Transactional
    public void deposit(UUID accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);

        if (!account.isActive()) {
            throw new AccountInactiveException();
        }

        AccountBehavior behavior = behaviorFactory.getBehavior(account.getType());
        behavior.validateDeposit(account, amount);
        account.deposit(amount);
    }

    /**
     * Atualiza o status da conta
     * @param accountId ID da conta
     * @param dto dados com novo status
     * @return DTO da conta atualizada
     */
    @Transactional
    public AccountResponseDTO update(UUID accountId, UpdateAccountRequestDTO dto) {
        Account account = findAccountById(accountId);
        if (dto.status() != null) {
            log.info("Updating account status {} to {}", accountId, dto.status());
            account.updateStatus(dto.status());
        }
        return AccountMapper.toResponseDTO(account);
    }

    /**
     * Desativa a conta
     * @param accountId ID da conta
     */
    @Transactional
    public void deactivate(UUID accountId) {
        Account account = findAccountById(accountId);
        log.info("Deactivating account: {}", accountId);
        account.deactivate();
    }

    /**
     * Ativa a conta
     * @param accountId ID da conta
     */
    @Transactional
    public void activate(UUID accountId) {
        Account account = findAccountById(accountId);
        log.info("Activating account: {}", accountId);
        account.activate();
    }

    /**
     * Verifica se o usuário autenticado é dono da conta
     * @param accountId ID da conta
     * @param authentication dados de autenticação
     * @return true se for dono, false caso contrário
     */
    public boolean isAccountOwner(UUID accountId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }
        return accountRepository.findById(accountId)
                .map(account -> account.getUser().getId().equals(userPrincipal.getId()))
                .orElse(false);
    }

    private void validateCreateDTO(CreateAccountRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        validateId(dto.userId());
        if (dto.type() == null) {
            throw new IllegalArgumentException("Account type is required");
        }
    }

    private Account findAccountById(UUID id) {
        validateId(id);
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    private void validateUserHasNoAccount(UUID userId) {
        accountRepository.findByUserId(userId)
                .ifPresent(acc -> {
                    throw new AccountAlreadyExistsException(userId);
                });
    }

    private User findUserById(UUID id) {
        validateId(id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void validateId(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }

    private void validateString(Object value, String field) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            throw new BusinessException(field + " is required");
        }
    }

    private RuntimeException handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        if (message.contains("accounts_accountnumber_key") ||
                message.contains("UK_") && message.contains("accountNumber")) {
            return new AccountNumberAlreadyExistsException("Account number already exists");
        }
        if (message.contains("accounts_user_id_key") || message.contains("UK_user_id")) {
            return new AccountAlreadyExistsException(UUID.randomUUID());
        }
        log.error("Unexpected data integrity violation", ex);
        return new BusinessException("Data integrity error. Please check unique fields.");
    }
}