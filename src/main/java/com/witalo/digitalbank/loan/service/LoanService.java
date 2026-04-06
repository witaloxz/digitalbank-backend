package com.witalo.digitalbank.loan.service;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.AccountNotFoundException;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.common.exception.BusinessException;
import com.witalo.digitalbank.loan.dto.*;
import com.witalo.digitalbank.loan.entity.Installment;
import com.witalo.digitalbank.loan.entity.Loan;
import com.witalo.digitalbank.loan.enums.InstallmentStatus;
import com.witalo.digitalbank.loan.enums.LoanStatus;
import com.witalo.digitalbank.loan.repository.InstallmentRepository;
import com.witalo.digitalbank.loan.repository.LoanRepository;
import com.witalo.digitalbank.notification.service.NotificationService;
import com.witalo.digitalbank.transaction.entity.Transaction;
import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;
import com.witalo.digitalbank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por todas as operações relacionadas a empréstimos.
 * Gerencia solicitação, aprovação, geração de parcelas e pagamentos.
 *
 * @author BankDash Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final InstallmentRepository installmentRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    /**
     * Busca empréstimos de uma conta com paginação
     * @param accountId ID da conta
     * @param pageable paginação
     * @return página de empréstimos
     */
    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> getLoansByAccount(UUID accountId, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return loanRepository.findByAccountId(accountId, pageable)
                .map(this::toResponseDTO);
    }

    /**
     * Solicita um novo empréstimo
     * @param accountId ID da conta
     * @param dto dados da solicitação
     * @return DTO do empréstimo criado
     */
    @Transactional
    public LoanResponseDTO requestLoan(UUID accountId, CreateLoanRequestDTO dto) {
        log.info("Solicitando empréstimo para conta: {} - Valor: {}", accountId, dto.amount());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // Cálculo da parcela mensal (amortização)
        BigDecimal monthlyInterestRate = dto.interestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_EVEN);

        double pow = Math.pow(1 + monthlyInterestRate.doubleValue(), dto.termMonths());
        BigDecimal monthlyPayment = dto.amount()
                .multiply(monthlyInterestRate)
                .multiply(BigDecimal.valueOf(pow))
                .divide(BigDecimal.valueOf(pow - 1), 2, RoundingMode.HALF_EVEN);

        LocalDateTime dueDate = LocalDateTime.now().plusMonths(dto.termMonths());

        Loan loan = Loan.builder()
                .account(account)
                .name(dto.name())
                .totalAmount(dto.amount())
                .remainingAmount(dto.amount())
                .interestRate(dto.interestRate())
                .monthlyPayment(monthlyPayment)
                .dueDate(dueDate)
                .status(LoanStatus.PENDING)
                .progressPercentage(0)
                .build();

        Loan saved = loanRepository.save(loan);
        log.info("Empréstimo solicitado com sucesso. ID: {}", saved.getId());

        return toResponseDTO(saved);
    }

    /**
     * Retorna o resumo dos empréstimos de uma conta
     * @param accountId ID da conta
     * @return DTO com o resumo
     */
    @Transactional(readOnly = true)
    public LoanSummaryDTO getLoanSummary(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        BigDecimal totalLoans = loanRepository.sumRemainingAmountByAccountId(accountId);
        BigDecimal monthlyPayment = loanRepository.sumMonthlyPaymentByAccountId(accountId);
        BigDecimal avgInterestRate = loanRepository.avgInterestRateByAccountId(accountId);

        return new LoanSummaryDTO(totalLoans, monthlyPayment, avgInterestRate);
    }

    /**
     * Busca empréstimo por ID
     * @param loanId ID do empréstimo
     * @return DTO do empréstimo
     */
    @Transactional(readOnly = true)
    public LoanResponseDTO getLoanById(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessException("Loan not found"));
        return toResponseDTO(loan);
    }

    /**
     * Lista empréstimos pendentes de aprovação (Admin)
     * @param pageable paginação
     * @return página de empréstimos pendentes
     */
    @Transactional(readOnly = true)
    public Page<AdminLoanDTO> getPendingLoans(Pageable pageable) {
        return loanRepository.findByStatus(LoanStatus.PENDING, pageable)
                .map(loan -> new AdminLoanDTO(
                        loan.getId(),
                        loan.getAccount().getUser().getName(),
                        loan.getAccount().getUser().getEmail(),
                        loan.getName(),
                        loan.getTotalAmount(),
                        loan.getInterestRate(),
                        loan.getProgressPercentage(),
                        loan.getStatus().name(),
                        loan.getCreatedAt()
                ));
    }

    /**
     * Aprova um empréstimo pendente (Admin)
     * @param loanId ID do empréstimo
     * @return DTO do empréstimo aprovado
     */
    @Transactional
    public LoanResponseDTO approveLoan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessException("Only pending loans can be approved");
        }

        Account account = loan.getAccount();
        account.deposit(loan.getTotalAmount());

        Transaction depositTransaction = new Transaction(
                account,
                TransactionType.DEPOSIT,
                loan.getTotalAmount(),
                "Approved loan: " + loan.getName(),
                null,
                TransactionStatus.SUCCESS,
                account.getBalance()
        );
        transactionRepository.save(depositTransaction);

        loan.setStatus(LoanStatus.APPROVED);
        generateInstallments(loan);
        loan.setStatus(LoanStatus.ACTIVE);

        String message = String.format("Your loan '%s' of R$ %.2f has been APPROVED! Installments are now available.",
                loan.getName(), loan.getTotalAmount());
        notificationService.sendNotification(loan.getAccount().getUser(), "Loan Approved", message, "LOAN_APPROVED");

        return toResponseDTO(loan);
    }

    /**
     * Rejeita um empréstimo pendente (Admin)
     * @param loanId ID do empréstimo
     */
    @Transactional
    public void rejectLoan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessException("Only pending loans can be rejected");
        }

        loan.setStatus(LoanStatus.DEFAULTED);

        String message = String.format("Unfortunately your loan '%s' of R$ %.2f has been REJECTED.",
                loan.getName(), loan.getTotalAmount());
        notificationService.sendNotification(loan.getAccount().getUser(), "Loan Rejected", message, "LOAN_REJECTED");
    }

    /**
     * Gera as parcelas do empréstimo após aprovação
     */
    private void generateInstallments(Loan loan) {
        if (installmentRepository.existsByLoanId(loan.getId())) {
            return;
        }

        BigDecimal monthly = loan.getMonthlyPayment();
        BigDecimal total = loan.getTotalAmount();
        int installments = total.divide(monthly, 0, RoundingMode.CEILING).intValue();
        LocalDate startDate = loan.getCreatedAt().toLocalDate().plusMonths(1);

        for (int i = 1; i <= installments; i++) {
            LocalDate dueDate = startDate.plusMonths(i - 1);
            String boletoCode = generateBoletoCode(loan.getId(), i);

            Installment installment = Installment.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .amount(monthly)
                    .dueDate(dueDate)
                    .boletoCode(boletoCode)
                    .status(InstallmentStatus.PENDING)
                    .build();
            installmentRepository.save(installment);
        }

        loan.setProgressPercentage(0);
    }

    /**
     * Gera código único para o boleto
     */
    private String generateBoletoCode(UUID loanId, int installmentNumber) {
        String uniqueId = UUID.randomUUID().toString().substring(0, 4);
        return "BL-" + loanId.toString().substring(0, 8) + "-" + installmentNumber + "-" + uniqueId;
    }

    /**
     * Paga uma parcela via código de boleto
     * @param boletoCode código do boleto
     */
    @Transactional
    public void payInstallment(String boletoCode) {
        Installment installment = installmentRepository.findByBoletoCode(boletoCode)
                .orElseThrow(() -> new BusinessException("Boleto not found"));

        if (installment.getStatus() != InstallmentStatus.PENDING) {
            throw new BusinessException("Boleto already paid or invalid");
        }

        Account account = installment.getLoan().getAccount();
        account.withdraw(installment.getAmount());

        Transaction withdrawTransaction = new Transaction(
                account,
                TransactionType.WITHDRAW,
                installment.getAmount(),
                "Payment of installment " + installment.getInstallmentNumber() + " of loan " + installment.getLoan().getName(),
                null,
                TransactionStatus.SUCCESS,
                account.getBalance()
        );
        transactionRepository.save(withdrawTransaction);

        installment.markAsPaid();

        // Atualiza o saldo restante e progresso do empréstimo
        Loan loan = installment.getLoan();
        BigDecimal newRemaining = loan.getRemainingAmount().subtract(installment.getAmount());
        loan.setRemainingAmount(newRemaining);

        BigDecimal total = loan.getTotalAmount();
        BigDecimal paid = total.subtract(newRemaining);
        int progress = paid.multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.DOWN).intValue();
        loan.setProgressPercentage(progress);

        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.PAID);
        }

        loanRepository.save(loan);
    }

    /**
     * Busca parcelas de um empréstimo
     * @param loanId ID do empréstimo
     * @param accountId ID da conta (para validação)
     * @return lista de parcelas
     */
    @Transactional(readOnly = true)
    public List<InstallmentDTO> getInstallmentsByLoan(UUID loanId, UUID accountId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessException("Loan not found"));

        if (!loan.getAccount().getId().equals(accountId)) {
            throw new BusinessException("Access denied");
        }

        return installmentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId)
                .stream()
                .map(this::toInstallmentDTO)
                .toList();
    }

    /**
     * Converte Installment para InstallmentDTO
     */
    private InstallmentDTO toInstallmentDTO(Installment inst) {
        return new InstallmentDTO(
                inst.getId(),
                inst.getInstallmentNumber(),
                inst.getAmount(),
                inst.getDueDate(),
                inst.getBoletoCode(),
                inst.getStatus().name(),
                inst.getPaidAt() != null ? inst.getPaidAt().toLocalDate() : null
        );
    }

    /**
     * Converte Loan para LoanResponseDTO
     */
    private LoanResponseDTO toResponseDTO(Loan loan) {
        return new LoanResponseDTO(
                loan.getId(),
                loan.getName(),
                loan.getTotalAmount(),
                loan.getRemainingAmount(),
                loan.getInterestRate(),
                loan.getMonthlyPayment(),
                loan.getDueDate(),
                loan.getStatus().name(),
                loan.getProgressPercentage()
        );
    }
}