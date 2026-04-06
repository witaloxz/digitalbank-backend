package com.witalo.digitalbank.common.security;

import com.witalo.digitalbank.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Implementação do UserDetails do Spring Security para representar o usuário autenticado.
 * Contém informações adicionais como ID do usuário e ID da conta.
 *
 * @author BankDash Team
 */
public class UserPrincipal implements UserDetails {

    @Getter
    private final UUID id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    @Getter
    private final UUID accountId;

    public UserPrincipal(User user, UUID accountId) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        this.accountId = accountId;
    }

    public UserPrincipal(UUID id, String email, String role, UUID accountId) {
        this.id = id;
        this.email = email;
        this.password = null;
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
        this.accountId = accountId;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}