package com.witalo.digitalbank.auth.service;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.auth.dto.AuthResponseDTO;
import com.witalo.digitalbank.auth.dto.LoginRequestDTO;
import com.witalo.digitalbank.auth.exception.InvalidCredentialsException;
import com.witalo.digitalbank.auth.exception.UserNotFoundException;
import com.witalo.digitalbank.common.security.JwtService;
import com.witalo.digitalbank.common.security.UserPrincipal;
import com.witalo.digitalbank.user.entity.User;
import com.witalo.digitalbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço responsável pela autenticação de usuários.
 * Gerencia login e geração de tokens JWT.
 *
 * @author BankDash Team
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Realiza o login do usuário
     * @param dto credenciais de login (email e senha)
     * @return DTO com token JWT
     * @throws UserNotFoundException se o usuário não for encontrado
     * @throws InvalidCredentialsException se a senha estiver incorreta
     */
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UserNotFoundException(dto.email()));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        UUID accountId = accountRepository.findByUserId(user.getId())
                .map(Account::getId)
                .orElse(null);

        UserPrincipal userPrincipal = new UserPrincipal(user, accountId);
        String token = jwtService.generateToken(userPrincipal);

        return new AuthResponseDTO(token, null);
    }
}