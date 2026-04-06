package com.witalo.digitalbank.transaction.service;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.entity.TransferKey;
import com.witalo.digitalbank.account.exception.AccountInactiveException;
import com.witalo.digitalbank.account.exception.AccountNotFoundException;
import com.witalo.digitalbank.account.exception.TransferKeyNotFoundException;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.account.repository.TransferKeyRepository;
import com.witalo.digitalbank.auth.exception.UserNotFoundException;
import com.witalo.digitalbank.notification.service.NotificationService;
import com.witalo.digitalbank.common.security.EncryptionService;
import com.witalo.digitalbank.transaction.dto.RecentContactDTO;
import com.witalo.digitalbank.transaction.dto.TransferRequestDTO;
import com.witalo.digitalbank.transaction.dto.TransferResponseDTO;
import com.witalo.digitalbank.transaction.entity.Transaction;
import com.witalo.digitalbank.transaction.entity.Transfer;
import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;
import com.witalo.digitalbank.account.enums.TransferKeyType;
import com.witalo.digitalbank.transaction.enums.TransferStatus;
import com.witalo.digitalbank.transaction.exception.TransactionBusinessException;
import com.witalo.digitalbank.transaction.repository.TransactionRepository;
import com.witalo.digitalbank.transaction.repository.TransferRepository;
import com.witalo.digitalbank.user.entity.User;
import com.witalo.digitalbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

/**
 * Serviço responsável por operações de transferência entre contas.
 * Suporta transferências via número da conta ou chave Pix, com controle de idempotência.
 *
 * @author BankDash Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final TransferKeyRepository transferKeyRepository;
    private final EncryptionService encryptionService;
    private final NotificationService notificationService;

    /**
     * Realiza uma transferência entre contas
     * @param dto dados da transferência
     * @param idempotencyKey chave única para evitar duplicação
     * @return DTO com os dados da transferência realizada
     * @throws TransactionBusinessException se a transferência falhar
     */
    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO dto, String idempotencyKey) {
        log.info("Starting transfer from {} of amount {}", dto.fromAccountId(), dto.amount());

        // Verifica se a transferência já foi processada (idempotência)
        if (transferRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            log.info("Transfer already processed for key {}", idempotencyKey);
            Transfer existing = transferRepository.findByIdempotencyKey(idempotencyKey).get();
            Account toAccount = accountRepository.findById(existing.getToAccountId())
                    .orElseThrow(() -> new AccountNotFoundException(existing.getToAccountId()));
            return new TransferResponseDTO(
                    existing.getId(),
                    toAccount.getAccountNumber(),
                    toAccount.getUser().getName(),
                    existing.getAmount(),
                    dto.description(),
                    existing.getCreatedAt()
            );
        }

        // Resolve conta de destino
        Account toAccount = resolveDestinationAccount(dto);
        if (!toAccount.isActive()) {
            throw new AccountInactiveException();
        }

        // Previne transferência para si mesmo
        if (dto.fromAccountId().equals(toAccount.getId())) {
            throw new TransactionBusinessException("You cannot transfer to yourself");
        }

        // Ordena IDs para evitar deadlock
        List<UUID> accountIds = new ArrayList<>(List.of(dto.fromAccountId(), toAccount.getId()));
        accountIds.sort(Comparator.naturalOrder());

        // Adquire locks na ordem
        Account first = accountRepository.findByIdWithLock(accountIds.get(0))
                .orElseThrow(() -> new AccountNotFoundException(accountIds.get(0)));
        Account second = accountRepository.findByIdWithLock(accountIds.get(1))
                .orElseThrow(() -> new AccountNotFoundException(accountIds.get(1)));

        // Identifica origem e destino após ordenação
        Account fromAccount = dto.fromAccountId().equals(first.getId()) ? first : second;
        Account toAccountLocked = dto.fromAccountId().equals(first.getId()) ? second : first;

        if (!fromAccount.isActive()) {
            throw new AccountInactiveException();
        }

        // Cria registro da transferência
        Transfer transfer = new Transfer(idempotencyKey, dto.fromAccountId(), toAccount.getId(), dto.amount());
        transferRepository.save(transfer);

        try {
            // Executa débito e crédito
            fromAccount.withdraw(dto.amount());
            toAccountLocked.deposit(dto.amount());

            // Cria transações
            Transaction withdrawTransaction = new Transaction(
                    fromAccount,
                    TransactionType.TRANSFER,
                    dto.amount(),
                    dto.description(),
                    transfer.getId(),
                    TransactionStatus.SUCCESS,
                    fromAccount.getBalance()
            );
            Transaction depositTransaction = new Transaction(
                    toAccountLocked,
                    TransactionType.DEPOSIT,
                    dto.amount(),
                    dto.description(),
                    transfer.getId(),
                    TransactionStatus.SUCCESS,
                    toAccountLocked.getBalance()
            );
            transactionRepository.saveAll(List.of(withdrawTransaction, depositTransaction));

            transfer.markAsCompleted();
            log.info("Transfer completed successfully. ID: {}", transfer.getId());

            String formattedAmount = formatCurrency(dto.amount());

            // Notifica o destinatário
            notificationService.sendNotification(
                    toAccount.getUser(),
                    "Transfer received",
                    String.format("You received %s of %s", formattedAmount, fromAccount.getUser().getName()),
                    "TRANSFER_RECEIVED"
            );

            return new TransferResponseDTO(
                    transfer.getId(),
                    toAccount.getAccountNumber(),
                    toAccount.getUser().getName(),
                    dto.amount(),
                    dto.description(),
                    transfer.getCreatedAt()
            );
        } catch (Exception ex) {
            log.error("Transfer failed: {}", ex.getMessage());
            transfer.markAsFailed();
            throw new TransactionBusinessException("Transfer failed: " + ex.getMessage());
        }
    }

    /**
     * Busca contatos recentes para transferência rápida
     * @param accountId ID da conta
     * @return lista de contatos recentes (máximo 5)
     */
    @Transactional(readOnly = true)
    public List<RecentContactDTO> getRecentContacts(UUID accountId) {
        List<Transfer> recentTransfers = transferRepository
                .findTop5ByFromAccountIdOrderByCreatedAtDesc(accountId);

        Map<UUID, RecentContactDTO> contactMap = new LinkedHashMap<>();

        for (Transfer transfer : recentTransfers) {
            UUID destAccountId = transfer.getToAccountId();
            if (contactMap.containsKey(destAccountId)) continue;

            Account destAccount = accountRepository.findById(destAccountId)
                    .orElseThrow(() -> new AccountNotFoundException(destAccountId));
            User destUser = destAccount.getUser();

            String name = destUser.getName();
            String accountNumber = destAccount.getAccountNumber();
            String avatarLetter = name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?";

            contactMap.put(destAccountId, new RecentContactDTO(destAccountId, name, accountNumber, avatarLetter));
            if (contactMap.size() == 5) break;
        }

        return new ArrayList<>(contactMap.values());
    }

    /**
     * Reverte uma transferência (estorno)
     * @param transferId ID da transferência
     * @param userDetails dados do usuário autenticado
     * @throws TransactionBusinessException se a transferência não puder ser revertida
     */
    @Transactional
    public void reverseTransfer(UUID transferId, UserDetails userDetails) {
        log.info("Reverse requested for transfer: {}", transferId);

        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransactionBusinessException("Transfer not found"));

        if (transfer.getStatus() != TransferStatus.COMPLETED) {
            throw new TransactionBusinessException("Only completed transfers can be reversed");
        }

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UUID toAccountId = transfer.getToAccountId();
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException(toAccountId));

        // Apenas o destinatário pode reverter
        if (!toAccount.getUser().getId().equals(currentUser.getId())) {
            throw new TransactionBusinessException("Only the recipient can reverse the transfer");
        }

        if (!toAccount.isActive()) {
            throw new AccountInactiveException();
        }

        List<Transaction> transactions = transactionRepository.findByTransferId(transferId);
        if (transactions.size() != 2) {
            throw new TransactionBusinessException("Incomplete transfer, cannot be reversed");
        }

        Transaction depositTx = transactions.stream()
                .filter(tx -> tx.getType() == TransactionType.DEPOSIT)
                .findFirst()
                .orElseThrow(() -> new TransactionBusinessException("Deposit transaction not found"));

        Transaction withdrawTx = transactions.stream()
                .filter(tx -> tx.getType() == TransactionType.WITHDRAW || tx.getType() == TransactionType.TRANSFER)
                .findFirst()
                .orElseThrow(() -> new TransactionBusinessException("Withdrawal/Transfer transaction not found"));

        // Adquire locks para evitar deadlock
        UUID fromId = transfer.getFromAccountId();
        UUID toId = transfer.getToAccountId();
        List<UUID> accountIds = new ArrayList<>(List.of(fromId, toId));
        accountIds.sort(Comparator.naturalOrder());

        Account first = accountRepository.findByIdWithLock(accountIds.get(0))
                .orElseThrow(() -> new AccountNotFoundException(accountIds.get(0)));
        Account second = accountRepository.findByIdWithLock(accountIds.get(1))
                .orElseThrow(() -> new AccountNotFoundException(accountIds.get(1)));

        Account fromAccount = fromId.equals(first.getId()) ? first : second;
        Account toAccountLocked = fromId.equals(first.getId()) ? second : first;

        // Reverte as transações
        depositTx.reverse();
        withdrawTx.reverse();

        transactionRepository.saveAll(List.of(depositTx, withdrawTx));

        transfer.markAsReversed();
        transferRepository.save(transfer);

        String formattedAmount = formatCurrency(transfer.getAmount());

        // Notifica o remetente
        String senderMessage = String.format("A transfer from %s you sent to %s has been rolled back. The amount was credited back to your account.",
                formattedAmount, toAccount.getUser().getName());
        notificationService.sendNotification(fromAccount.getUser(), "Reverted transfer", senderMessage, "TRANSFER_REVERSED");

        // Notifica o destinatário
        String recipientMessage = String.format("You reverted the transfer of %s sent by %s. The amount was debited from your account.",
                formattedAmount, fromAccount.getUser().getName());
        notificationService.sendNotification(toAccount.getUser(), "Reverted transfer", recipientMessage, "TRANSFER_REVERSED");

        log.info("Transfer {} reversed successfully", transferId);
    }

    /**
     * Resolve a conta de destino a partir do número da conta ou chave Pix
     */
    private Account resolveDestinationAccount(TransferRequestDTO dto) {
        if (dto.destinationAccountNumber() != null && !dto.destinationAccountNumber().isBlank()) {
            return accountRepository.findByAccountNumber(dto.destinationAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(dto.destinationAccountNumber()));
        }
        if (dto.transferKey() != null && !dto.transferKey().isBlank() && dto.transferKeyType() != null) {
            String normalizedKey = normalizeKey(dto.transferKeyType(), dto.transferKey());
            TransferKey key = transferKeyRepository.findByTypeAndValue(dto.transferKeyType(), normalizedKey)
                    .orElseThrow(() -> new TransferKeyNotFoundException(dto.transferKeyType(), dto.transferKey()));
            return key.getAccount();
        }
        throw new IllegalArgumentException("Either destination account number or transfer key must be provided");
    }

    /**
     * Normaliza a chave de transferência conforme o tipo
     */
    private String normalizeKey(TransferKeyType type, String value) {
        if (type == TransferKeyType.EMAIL) {
            return value.trim().toLowerCase();
        } else if (type == TransferKeyType.PHONE) {
            return value.replaceAll("\\D", "");
        } else if (type == TransferKeyType.CPF) {
            String clean = value.replaceAll("\\D", "");
            return encryptionService.encrypt(clean);
        }
        return value;
    }

    /**
     * Formata valor como moeda brasileira
     */
    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return currencyFormat.format(amount);
    }
}