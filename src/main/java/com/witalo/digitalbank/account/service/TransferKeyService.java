package com.witalo.digitalbank.account.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.witalo.digitalbank.account.dto.CreateTransferKeyRequestDTO;
import com.witalo.digitalbank.account.dto.TransferKeyResponseDTO;
import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.entity.TransferKey;
import com.witalo.digitalbank.account.enums.TransferKeyType;
import com.witalo.digitalbank.account.exception.AccountNotFoundException;
import com.witalo.digitalbank.account.exception.TransferKeyAlreadyExistsException;
import com.witalo.digitalbank.account.exception.TransferKeyNotFoundException;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.account.repository.TransferKeyRepository;
import com.witalo.digitalbank.common.exception.BusinessException;
import com.witalo.digitalbank.common.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por operações de chaves de transferência (Pix).
 * Gerencia criação, listagem e remoção de chaves.
 *
 * @author BankDash Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferKeyService {

    private final TransferKeyRepository keyRepository;
    private final AccountRepository accountRepository;
    private final EncryptionService encryptionService;

    /**
     * Cria uma nova chave de transferência para a conta
     * @param accountId ID da conta
     * @param dto dados da chave (tipo e valor)
     * @return DTO da chave criada
     */
    @Transactional
    public TransferKeyResponseDTO create(UUID accountId, CreateTransferKeyRequestDTO dto) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        validateKey(dto.type(), dto.value(), account);

        String normalizedValue = normalizeValue(dto.type(), dto.value());

        if (keyRepository.existsByTypeAndValue(dto.type(), normalizedValue)) {
            throw new TransferKeyAlreadyExistsException(dto.type(), dto.value());
        }

        // Validação específica para CPF (apenas uma chave por conta)
        if (dto.type() == TransferKeyType.CPF) {
            boolean alreadyHasCpf = keyRepository.existsByAccountIdAndType(accountId, TransferKeyType.CPF);
            if (alreadyHasCpf) {
                throw new BusinessException("You already have a CPF Pix key registered");
            }
        }

        TransferKey key = new TransferKey(dto.type(), normalizedValue, account);
        TransferKey saved = keyRepository.save(key);
        log.info("Transfer key created for account {}: {} - {}", accountId, dto.type(), dto.value());
        return toResponseDTO(saved);
    }

    /**
     * Lista todas as chaves de transferência de uma conta
     * @param accountId ID da conta
     * @return lista de chaves
     */
    @Transactional(readOnly = true)
    public List<TransferKeyResponseDTO> listByAccount(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return keyRepository.findByAccountId(accountId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Remove uma chave de transferência
     * @param keyId ID da chave
     * @param accountId ID da conta (para validação)
     */
    @Transactional
    public void delete(UUID keyId, UUID accountId) {
        TransferKey key = keyRepository.findById(keyId)
                .orElseThrow(() -> new TransferKeyNotFoundException(null, null));

        if (!key.getAccount().getId().equals(accountId)) {
            throw new IllegalArgumentException("Key does not belong to this account");
        }

        keyRepository.delete(key);
        log.info("Transfer key deleted: {}", keyId);
    }

    /**
     * Valida a chave conforme seu tipo
     * @param type tipo da chave
     * @param value valor da chave
     * @param account conta associada
     */
    private void validateKey(TransferKeyType type, String value, Account account) {
        if (type == TransferKeyType.CPF) {
            String cleanCpf = value.replaceAll("\\D", "");
            String userCpfEncrypted = account.getUser().getCpf();
            String userCpfDecrypted = encryptionService.decrypt(userCpfEncrypted);
            if (!userCpfDecrypted.equals(cleanCpf)) {
                throw new BusinessException("The provided CPF does not match your account's CPF");
            }
        } else if (type == TransferKeyType.EMAIL) {
            EmailValidator validator = EmailValidator.getInstance();
            if (!validator.isValid(value)) {
                throw new BusinessException("Invalid email format. Example: user@domain.com");
            }
        } else if (type == TransferKeyType.PHONE) {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                var number = phoneUtil.parse(value, "BR");
                if (!phoneUtil.isValidNumber(number)) {
                    throw new BusinessException("Invalid phone number");
                }
            } catch (NumberParseException e) {
                throw new BusinessException("Invalid phone number. Use international format (e.g., +5511999999999)");
            }
        }
    }

    /**
     * Normaliza o valor da chave conforme seu tipo
     * @param type tipo da chave
     * @param value valor original
     * @return valor normalizado
     */
    private String normalizeValue(TransferKeyType type, String value) {
        if (type == TransferKeyType.EMAIL) {
            return value.trim().toLowerCase();
        } else if (type == TransferKeyType.PHONE) {
            return value.replaceAll("\\D", "");
        } else if (type == TransferKeyType.CPF) {
            String clean = value.replaceAll("\\D", "");
            return encryptionService.encrypt(clean);
        }
        return value;
    }

    /**
     * Converte TransferKey para TransferKeyResponseDTO
     */
    private TransferKeyResponseDTO toResponseDTO(TransferKey key) {
        String displayValue = key.getValue();
        if (key.getType() == TransferKeyType.CPF) {
            displayValue = encryptionService.decrypt(key.getValue());
        }
        return new TransferKeyResponseDTO(key.getId(), key.getType(), displayValue);
    }
}