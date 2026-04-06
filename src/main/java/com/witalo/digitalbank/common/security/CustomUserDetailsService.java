package com.witalo.digitalbank.common.security;

import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.user.entity.User;
import com.witalo.digitalbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço responsável por carregar os dados do usuário durante a autenticação.
 * Implementa a interface UserDetailsService do Spring Security.
 *
 * @author BankDash Team
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        UUID accountId = accountRepository.findByUserId(user.getId())
                .map(account -> account.getId())
                .orElse(null);

        return new UserPrincipal(user, accountId);
    }
}