package com.witalo.digitalbank.account.mapper;

import com.witalo.digitalbank.account.dto.AccountResponseDTO;
import com.witalo.digitalbank.account.entity.Account;

/**
 * Mapper responsável por converter entidades Account em DTOs de resposta.
 *
 * @author BankDash Team
 */
public class AccountMapper {

    private AccountMapper() {
        // Construtor privado para evitar instanciação
    }

    /**
     * Converte entidade Account para AccountResponseDTO
     * @param account entidade da conta
     * @return DTO com os dados da conta
     */
    public static AccountResponseDTO toResponseDTO(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getAgency(),
                account.getAccountNumber(),
                account.getAccountDigit(),
                account.getType(),
                account.getBalance(),
                account.getStatus(),
                account.getUser().getId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}