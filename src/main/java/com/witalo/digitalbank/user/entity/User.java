package com.witalo.digitalbank.user.entity;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.user.enums.UserRole;
import com.witalo.digitalbank.user.enums.UserStatus;
import com.witalo.digitalbank.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa um usuário do sistema.
 * Contém informações pessoais, credenciais e relacionamento com conta e preferências.
 *
 * @author BankDash Team
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'USER'")
    private UserRole role = UserRole.USER;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(nullable = false, unique = true, length = 255)
    private String cpf;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user")
    private Account account;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPreferences preferences;

    public User(String name, LocalDate dateOfBirth, String email, String phone, String cpf, String password) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phone = phone;
        this.cpf = cpf;
        this.password = password;
        this.status = UserStatus.ACTIVE;
        this.role = UserRole.USER;
    }

    /**
     * Valida e atualiza o nome do usuário
     * @param name novo nome
     * @throws BusinessException se nome for inválido ou exceder o limite
     */
    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Name is invalid");
        }
        if (name.length() > 120) {
            throw new BusinessException("Name must not exceed 120 characters");
        }
        this.name = name;
    }

    /**
     * Valida e atualiza a senha do usuário
     * @param password nova senha
     * @throws BusinessException se senha for inválida ou muito curta
     */
    public void updatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("Password is invalid");
        }
        if (password.length() < 6) {
            throw new BusinessException("Password must be at least 6 characters long");
        }
        this.password = password;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    /**
     * Desativa a conta do usuário
     * @throws BusinessException se o usuário já estiver inativo
     */
    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new BusinessException("User is already inactive");
        }
        this.status = UserStatus.INACTIVE;
    }

    /**
     * Ativa a conta do usuário
     * @throws BusinessException se o usuário já estiver ativo
     */
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new BusinessException("User is already active");
        }
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Associa uma conta ao usuário
     * @param account conta a ser associada
     * @throws BusinessException se o usuário já possuir uma conta
     */
    public void assignAccount(Account account) {
        if (this.account != null) {
            throw new com.witalo.digitalbank.common.exception.BusinessException("User already has an account");
        }
        this.account = account;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}