package com.witalo.digitalbank.user.repository;

import com.witalo.digitalbank.user.enums.UserRole;
import com.witalo.digitalbank.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de persistência de usuários.
 *
 * @author BankDash Team
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * Busca usuário por e-mail
     * @param email e-mail do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca usuário por CPF
     * @param cpf CPF do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<User> findByCpf(String cpf);

    /**
     * Busca usuários por role
     * @param userRole role do usuário (USER, ADMIN)
     * @return coleção de usuários com a role especificada
     */
    Collection<User> findByRole(UserRole userRole);

    /**
     * Busca todos os usuários com seus relacionamentos (conta e preferências)
     * @param pageable paginação
     * @return página de usuários com account e preferences carregados
     */
    @Query("SELECT u FROM User u")
    @EntityGraph(attributePaths = {"account", "preferences"})
    Page<User> findAllWithAccountAndPreferences(Pageable pageable);

    /**
     * Busca usuários com filtros e relacionamentos
     * @param spec specification para filtros dinâmicos
     * @param pageable paginação
     * @return página de usuários com account e preferences carregados
     */
    @EntityGraph(attributePaths = {"account", "preferences"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    /**
     * Conta quantidade de usuários criados por mês em um período
     * @param startDate data inicial do período
     * @param endDate data final do período
     * @return lista de arrays [mês, quantidade]
     */
    @Query("SELECT FUNCTION('TO_CHAR', u.createdAt, 'Mon') as month, COUNT(u) as count " +
            "FROM User u " +
            "WHERE u.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY month " +
            "ORDER BY MIN(u.createdAt)")
    List<Object[]> countUsersByMonth(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

}