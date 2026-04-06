package com.witalo.digitalbank.insurance.service;

import com.witalo.digitalbank.account.entity.Account;
import com.witalo.digitalbank.account.exception.AccountNotFoundException;
import com.witalo.digitalbank.account.repository.AccountRepository;
import com.witalo.digitalbank.common.exception.BusinessException;
import com.witalo.digitalbank.insurance.dto.AdminInsuranceDTO;
import com.witalo.digitalbank.insurance.dto.InsuranceRequestDTO;
import com.witalo.digitalbank.insurance.dto.InsuranceResponseDTO;
import com.witalo.digitalbank.insurance.entity.Insurance;
import com.witalo.digitalbank.insurance.enums.InsurancePlan;
import com.witalo.digitalbank.insurance.enums.InsuranceStatus;
import com.witalo.digitalbank.insurance.repository.InsuranceRequestRepository;
import com.witalo.digitalbank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável por operações de seguro de vida.
 * Gerencia solicitações, aprovação e rejeição.
 *
 * @author BankDash Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsuranceRequestRepository insuranceRequestRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;

    /**
     * Solicita um seguro de vida
     * @param accountId ID da conta
     * @param dto dados da solicitação (plano)
     * @return DTO da solicitação criada
     */
    @Transactional
    public InsuranceResponseDTO requestLifeInsurance(UUID accountId, InsuranceRequestDTO dto) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        InsurancePlan plan = InsurancePlan.valueOf(dto.plan().toUpperCase());

        Insurance request = Insurance.builder()
                .account(account)
                .plan(plan)
                .build();

        Insurance saved = insuranceRequestRepository.save(request);

        log.info("Life insurance request created: {} for account {}", saved.getId(), accountId);

        return new InsuranceResponseDTO(
                saved.getId(),
                saved.getPlan().name(),
                saved.getStatus().name(),
                saved.getCreatedAt()
        );
    }

    /**
     * Lista solicitações de seguro pendentes (Admin)
     * @param pageable paginação
     * @return página de solicitações pendentes
     */
    @Transactional(readOnly = true)
    public Page<AdminInsuranceDTO> getPendingRequests(Pageable pageable) {
        return insuranceRequestRepository.findByStatus(InsuranceStatus.PENDING, pageable)
                .map(request -> new AdminInsuranceDTO(
                        request.getId(),
                        request.getAccount().getUser().getName(),
                        request.getAccount().getUser().getEmail(),
                        request.getPlan().name(),
                        request.getStatus().name(),
                        request.getCreatedAt()
                ));
    }

    /**
     * Aprova uma solicitação de seguro (Admin)
     * @param requestId ID da solicitação
     */
    @Transactional
    public void approveRequest(UUID requestId) {
        Insurance request = insuranceRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Insurance request not found"));

        request.approve();

        String message = String.format("Your life insurance request (%s plan) has been APPROVED!", request.getPlan().name());
        notificationService.sendNotification(request.getAccount().getUser(), "Life Insurance Approved", message, "INSURANCE_APPROVED");

        log.info("Insurance request approved: {}", requestId);
    }

    /**
     * Rejeita uma solicitação de seguro (Admin)
     * @param requestId ID da solicitação
     */
    @Transactional
    public void rejectRequest(UUID requestId) {
        Insurance request = insuranceRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Insurance request not found"));

        request.reject();

        String message = String.format("Unfortunately your life insurance request (%s plan) has been REJECTED.", request.getPlan().name());
        notificationService.sendNotification(request.getAccount().getUser(), "Life Insurance Rejected", message, "INSURANCE_REJECTED");

        log.info("Insurance request rejected: {}", requestId);
    }
}