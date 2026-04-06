package com.witalo.digitalbank.common.security;

import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.loan.repository.LoanRepository;
import com.witalo.digitalbank.transaction.repository.TransactionRepository;
import com.witalo.digitalbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

/**
 * Avaliador de permissões customizado para controle de acesso a recursos.
 * Verifica se o usuário autenticado tem permissão para acessar um recurso específico.
 *
 * @author BankDash Team
 */
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        // Admin tem acesso total a todos os recursos
        if (userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Converte targetId para UUID
        UUID target;
        try {
            target = (targetId instanceof UUID) ? (UUID) targetId : UUID.fromString(targetId.toString());
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Verifica permissão baseada no tipo do recurso
        return switch (targetType) {
            case "user" -> userPrincipal.getId().equals(target);
            case "account" -> accountRepository.findById(target)
                    .map(account -> account.getUser().getId().equals(userPrincipal.getId()))
                    .orElse(false);
            case "transaction" -> transactionRepository.findById(target)
                    .map(transaction -> transaction.getAccount().getUser().getId().equals(userPrincipal.getId()))
                    .orElse(false);
            case "loan" -> loanRepository.findById(target)
                    .map(loan -> loan.getAccount().getUser().getId().equals(userPrincipal.getId()))
                    .orElse(false);
            default -> false;
        };
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Não utilizado nesta aplicação
        return false;
    }
}