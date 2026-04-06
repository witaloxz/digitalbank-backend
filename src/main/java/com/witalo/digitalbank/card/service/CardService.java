package com.witalo.digitalbank.card.service;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.AccountNotFoundException;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.card.dto.CardResponseDTO;
import com.witalo.digitalbank.card.dto.CreateCardRequestDTO;
import com.witalo.digitalbank.card.entity.Card;
import com.witalo.digitalbank.card.enums.CardStatus;
import com.witalo.digitalbank.card.enums.CardType;
import com.witalo.digitalbank.card.exception.CardLimitExceededException;
import com.witalo.digitalbank.card.exception.CardNotFoundException;
import com.witalo.digitalbank.card.repository.CardRepository;
import com.witalo.digitalbank.common.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço responsável por operações de cartões bancários.
 * Gerencia criação, consulta, bloqueio e remoção de cartões.
 *
 * @author BankDash Team
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final EncryptionService encryptionService;

    private static final int MAX_CARDS_PER_ACCOUNT = 2;

    /**
     * Cria um novo cartão para a conta
     * @param accountId ID da conta
     * @param dto dados do cartão (tipo e limite)
     * @return DTO do cartão criado
     */
    @Transactional
    public CardResponseDTO createCard(UUID accountId, CreateCardRequestDTO dto) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // Limite máximo de cartões por conta
        long activeCardsCount = cardRepository.countByAccountIdAndStatusNot(accountId, CardStatus.DELETED);
        if (activeCardsCount >= MAX_CARDS_PER_ACCOUNT) {
            throw new CardLimitExceededException("Maximum of " + MAX_CARDS_PER_ACCOUNT + " cards per account");
        }

        // Verifica se já existe um cartão ativo do mesmo tipo
        if (cardRepository.existsByAccountIdAndTypeAndStatusNot(accountId, dto.type(), CardStatus.DELETED)) {
            throw new CardLimitExceededException("You already have an active " + dto.type() + " card");
        }

        String cardNumber = generateUniqueCardNumber();
        String cvv = generateCVV();
        LocalDate expiryDate = LocalDate.now().plusYears(3);

        Card card = new Card(
                account,
                cardNumber,
                encryptionService.encrypt(cvv),
                expiryDate,
                dto.type(),
                dto.creditLimit() != null ? dto.creditLimit() : BigDecimal.ZERO
        );

        card = cardRepository.save(card);
        return toResponseDTO(card);
    }

    /**
     * Lista todos os cartões ativos de uma conta
     * @param accountId ID da conta
     * @return lista de cartões
     */
    @Transactional(readOnly = true)
    public List<CardResponseDTO> getCardsByAccount(UUID accountId) {
        return cardRepository.findByAccountIdAndStatusNot(accountId, CardStatus.DELETED)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Bloqueia um cartão
     * @param cardId ID do cartão
     * @param accountId ID da conta (para validação)
     */
    @Transactional
    public void blockCard(UUID cardId, UUID accountId) {
        Card card = findCardByIdAndAccount(cardId, accountId);
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Card is already blocked");
        }
        card.block();
    }

    /**
     * Remove um cartão (soft delete)
     * @param cardId ID do cartão
     * @param accountId ID da conta (para validação)
     */
    @Transactional
    public void deleteCard(UUID cardId, UUID accountId) {
        Card card = findCardByIdAndAccount(cardId, accountId);
        if (card.getStatus() == CardStatus.DELETED) {
            throw new IllegalStateException("Card is already deleted");
        }
        card.delete();
    }

    private Card findCardByIdAndAccount(UUID cardId, UUID accountId) {
        return cardRepository.findById(cardId)
                .filter(card -> card.getAccount().getId().equals(accountId))
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private String generateUniqueCardNumber() {
        String cardNumber;
        do {
            cardNumber = String.format("%04d%04d%04d%04d",
                    (int) (Math.random() * 10000),
                    (int) (Math.random() * 10000),
                    (int) (Math.random() * 10000),
                    (int) (Math.random() * 10000));
        } while (cardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    private String generateCVV() {
        return String.format("%03d", (int) (Math.random() * 1000));
    }

    private CardResponseDTO toResponseDTO(Card card) {
        String decryptedCvv = encryptionService.decrypt(card.getCvv());
        return new CardResponseDTO(
                card.getId(),
                maskCardNumber(card.getCardNumber()),
                decryptedCvv,
                card.getExpiryDate(),
                card.getType(),
                card.getStatus(),
                card.getCreditLimit()
        );
    }

    private String maskCardNumber(String number) {
        if (number == null || number.length() < 8) return number;
        return number.substring(0, 4) + " **** **** " + number.substring(number.length() - 4);
    }
}