package com.witalo.digitalbank.transaction.specification;

import com.witalo.digitalbank.transaction.entity.Transaction;
import com.witalo.digitalbank.transaction.enums.TransactionStatus;
import com.witalo.digitalbank.transaction.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por construir Specifications dinâmicas para consulta de transações.
 * Permite filtrar por termo de busca, tipo e status.
 *
 * @author BankDash Team
 */
public class TransactionSpecifications {

    /**
     * Cria uma Specification com filtros dinâmicos
     * @param search termo para busca em descrição ou nome do usuário
     * @param type tipo da transação (DEPOSIT, WITHDRAW, TRANSFER)
     * @param status status da transação (SUCCESS, PENDING, FAILED)
     * @return Specification pronta para uso em consultas
     */
    public static Specification<Transaction> withFilters(String search, TransactionType type, TransactionStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por termo de busca (descrição ou nome do usuário)
            if (search != null && !search.isBlank()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), likePattern);
                Predicate accountUserPredicate = cb.like(
                        cb.lower(root.get("account").get("user").get("name")), likePattern);
                predicates.add(cb.or(descriptionPredicate, accountUserPredicate));
            }

            // Filtro por tipo de transação
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            // Filtro por status da transação
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}