package com.witalo.digitalbank.admin.service;

import com.witalo.digitalbank.admin.dto.*;
import com.witalo.digitalbank.loan.enums.LoanStatus;
import com.witalo.digitalbank.user.enums.UserRole;
import com.witalo.digitalbank.loan.entity.Loan;
import com.witalo.digitalbank.loan.repository.LoanRepository;
import com.witalo.digitalbank.common.security.EncryptionService;
import com.witalo.digitalbank.transaction.repository.TransactionRepository;
import com.witalo.digitalbank.user.entity.User;
import com.witalo.digitalbank.user.exception.UserNotFoundException;
import com.witalo.digitalbank.user.repository.UserRepository;
import com.witalo.digitalbank.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final EncryptionService encryptionService;

    @Transactional(readOnly = true)
    public AdminStatsDTO getStats() {
        long totalUsers = userRepository.count();
        long totalTransactions = transactionRepository.count();
        long totalPendingLoans = loanRepository.countByStatusPending(); // implementar no repositório
        BigDecimal totalRevenue = transactionRepository.sumAmountByTypeDepositAndStatusSuccess(); // implementar
        return new AdminStatsDTO(totalUsers, totalTransactions, totalPendingLoans, totalRevenue);
    }


    @Transactional
    public void updateUserRole(UUID userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setRole(newRole);
    }

    public Page<AdminUserResponseDTO> findAllUsers(String search, String status, String role, Pageable pageable) {
        Specification<User> spec = com.witalo.digitalbank.user.specification.UserSpecifications.withFilters(search, status, role);
        return userRepository.findAll(spec, pageable)
                .map(user -> {
                    BigDecimal balance = user.getAccount() != null ? user.getAccount().getBalance() : BigDecimal.ZERO;
                    return new AdminUserResponseDTO(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            encryptionService.decrypt(user.getCpf()),
                            user.getRole().name(),
                            user.getStatus().name(),
                            balance,
                            user.getCreatedAt()
                    );
                });
    }

    public void toggleUserStatus(UUID userId) {
        userService.toggleUserStatus(userId);
    }

    public List<MonthlyDataDTO> getMonthlyUserRegistrations(int monthsBack) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(monthsBack - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = now.atTime(23, 59, 59);

        List<Object[]> results = userRepository.countUsersByMonth(startDateTime, endDateTime);
        return results.stream()
                .map(row -> new MonthlyDataDTO((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<MonthlyDataDTO> getMonthlyTransactions(int monthsBack) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(monthsBack - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = now.atTime(23, 59, 59);

        List<Object[]> results = transactionRepository.countTransactionsByMonth(startDateTime, endDateTime);
        return results.stream()
                .map(row -> new MonthlyDataDTO((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<MonthlyDataDTO> getMonthlyRevenue(int monthsBack) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(monthsBack - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = now.atTime(23, 59, 59);

        List<Object[]> results = transactionRepository.sumRevenueByMonth(startDateTime, endDateTime);
        return results.stream()
                .map(row -> new MonthlyDataDTO((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<StatusDistributionDTO> getTransactionStatusDistribution() {
        List<Object[]> results = transactionRepository.countByStatus();
        return results.stream()
                .map(row -> new StatusDistributionDTO(
                        row[0].toString(),
                        ((Number) row[1]).longValue(),
                        getColorForStatus(row[0].toString())
                ))
                .collect(Collectors.toList());
    }

    private String getColorForStatus(String status) {
        return switch (status) {
            case "SUCCESS" -> "hsl(var(--success))";
            case "PENDING" -> "hsl(var(--warning))";
            case "FAILED" -> "hsl(var(--destructive))";
            default -> "hsl(var(--muted))";
        };
        // Outros métodos admin: bloquear/ativar usuário, listar transações, etc.
    }

    public Page<AdminLoanDTO> getAllLoans(String status, Pageable pageable) {
        Specification<Loan> spec = (root, query, cb) -> {
            if (status != null && !status.isBlank()) {
                return cb.equal(root.get("status"), LoanStatus.valueOf(status.toUpperCase()));
            }
            return cb.conjunction();
        };
        return loanRepository.findAll(spec, pageable)
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

    @Transactional
    public void updateLoanStatus(UUID loanId, LoanStatus newStatus) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(newStatus);
        // Se aprovado e antes estava PENDING, talvez queira manter os valores como estão
        // Se necessário, pode ativar o empréstimo (já está com total e remaining iguais)
    }
}