package com.witalo.digitalbank.user.specification;

import com.witalo.digitalbank.user.enums.UserRole;
import com.witalo.digitalbank.user.enums.UserStatus;
import com.witalo.digitalbank.user.entity.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por construir Specifications dinâmicas para consulta de usuários.
 * Permite filtrar por termo de busca, status e role.
 *
 * @author BankDash Team
 */
public class UserSpecifications {

    /**
     * Cria uma Specification com filtros dinâmicos
     * @param search termo para busca em nome ou e-mail
     * @param status status do usuário (ACTIVE, INACTIVE)
     * @param role role do usuário (USER, ADMIN)
     * @return Specification pronta para uso em consultas
     */
    public static Specification<User> withFilters(String search, String status, String role) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por termo de busca (nome ou e-mail)
            if (search != null && !search.isBlank()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("email")), likePattern)
                ));
            }

            // Filtro por status do usuário
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), UserStatus.valueOf(status.toUpperCase())));
            }

            // Filtro por role do usuário
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role"), UserRole.valueOf(role.toUpperCase())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}