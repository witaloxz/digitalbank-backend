package com.witalo.digitalbank.transaction.service;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.AccountInactiveException;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.common.exception.BusinessException;
import com.witalo.digitalbank.transaction.dto.AdminTransactionResponseDTO;
import com.witalo.digitalbank.transaction.dto.TransactionRequestDTO;
import com.witalo.digitalbank.transaction.dto.TransactionResponseDTO;
import com.witalo.digitalbank.transaction.entity.Transaction;
import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;
import com.witalo.digitalbank.transaction.exception.TransactionBusinessException;
import com.witalo.digitalbank.transaction.mapper.TransactionMapper;
import com.witalo.digitalbank.transaction.repository.TransactionRepository;
import com.witalo.digitalbank.transaction.specification.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável por todas as operações relacionadas a transações financeiras.
 * Gerencia depósitos, saques, consultas e reversões.
 *
 * @author BankDash Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Realiza um depósito em uma conta
     * @param dto dados da transação (accountId, amount, description)
     * @return DTO da transação realizada
     * @throws TransactionBusinessException se a conta não for encontrada
     * @throws AccountInactiveException se a conta estiver inativa
     */
    @Transactional
    public TransactionResponseDTO deposit(TransactionRequestDTO dto) {
        return processTransaction(dto, TransactionType.DEPOSIT);
    }

    /**
     * Realiza um saque em uma conta
     * @param dto dados da transação (accountId, amount, description)
     * @return DTO da transação realizada
     * @throws TransactionBusinessException se a conta não for encontrada ou saldo insuficiente
     * @throws AccountInactiveException se a conta estiver inativa
     */
    @Transactional
    public TransactionResponseDTO withdraw(TransactionRequestDTO dto) {
        return processTransaction(dto, TransactionType.WITHDRAW);
    }

    /**
     * Processa uma transação financeira (depósito ou saque)
     * @param dto dados da transação
     * @param type tipo da transação (DEPOSIT ou WITHDRAW)
     * @return DTO da transação processada
     */
    private TransactionResponseDTO processTransaction(TransactionRequestDTO dto, TransactionType type) {
        log.info("Processing {} of amount {} for account {}", type, dto.amount(), dto.accountId());

        Account account = accountRepository.findByIdWithLock(dto.accountId())
                .orElseThrow(() -> new TransactionBusinessException("Account not found"));

        if (!account.isActive()) {
            throw new AccountInactiveException();
        }

        try {
            if (type == TransactionType.DEPOSIT) {
                account.deposit(dto.amount());
            } else {
                account.withdraw(dto.amount());
            }

            Transaction transaction = new Transaction(
                    account,
                    type,
                    dto.amount(),
                    dto.description(),
                    null,
                    TransactionStatus.SUCCESS,
                    account.getBalance()
            );
            transactionRepository.save(transaction);

            log.info("{} successful. New balance: {}", type, account.getBalance());
            return TransactionMapper.toResponseDTO(transaction);
        } catch (Exception ex) {
            log.warn("Failed to process {}: {}", type, ex.getMessage());
            Transaction failedTransaction = new Transaction(
                    account,
                    type,
                    dto.amount(),
                    dto.description(),
                    null,
                    TransactionStatus.FAILED,
                    account.getBalance()
            );
            transactionRepository.save(failedTransaction);
            throw new TransactionBusinessException("Failed to process transaction: " + ex.getMessage());
        }
    }

    /**
     * Busca transações para o painel administrativo com filtros
     * @param search termo de busca
     * @param type tipo da transação
     * @param status status da transação
     * @param pageable paginação
     * @return página de DTOs administrativos
     */
    @Transactional(readOnly = true)
    public Page<AdminTransactionResponseDTO> findAdminTransactions(String search, TransactionType type, TransactionStatus status, Pageable pageable) {
        return transactionRepository.findAdminTransactions(search, type, status, pageable);
    }

    /**
     * Busca transações com paginação e filtros
     * @param search termo de busca
     * @param type tipo da transação
     * @param status status da transação
     * @param pageable paginação
     * @return página de DTOs de transação
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> findAllPaged(String search, TransactionType type, TransactionStatus status, Pageable pageable) {
        Specification<Transaction> spec = TransactionSpecifications.withFilters(search, type, status);
        return transactionRepository.findAll(spec, pageable)
                .map(TransactionMapper::toResponseDTO);
    }

    /**
     * Busca transação por ID
     * @param id identificador da transação
     * @return DTO da transação encontrada
     * @throws TransactionBusinessException se a transação não existir
     */
    @Transactional(readOnly = true)
    public TransactionResponseDTO findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionBusinessException("Transaction not found"));
        return TransactionMapper.toResponseDTO(transaction);
    }

    /**
     * Atualiza o status de uma transação (uso administrativo)
     * @param transactionId identificador da transação
     * @param newStatus novo status
     * @throws TransactionBusinessException se a transação não existir
     */
    @Transactional
    public void updateTransactionStatus(UUID transactionId, TransactionStatus newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionBusinessException("Transaction not found"));
        transaction.setStatus(newStatus);
    }

    /**
     * Reverte uma transação (estorno)
     * @param transactionId identificador da transação
     * @throws BusinessException se a transação não puder ser revertida
     */
    @Transactional
    public void reverseTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionBusinessException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new BusinessException("Only SUCCESS transactions can be reversed");
        }

        transaction.reverse();
        transactionRepository.save(transaction);
    }
}