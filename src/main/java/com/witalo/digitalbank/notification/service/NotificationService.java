package com.witalo.digitalbank.notification.service;

import com.witalo.digitalbank.notification.dto.NotificationDTO;
import com.witalo.digitalbank.notification.entity.Notification;
import com.witalo.digitalbank.notification.repository.NotificationRepository;
import com.witalo.digitalbank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pelo gerenciamento de notificações do usuário.
 * Envia notificações em tempo real via WebSocket e persiste no banco de dados.
 *
 * @author BankDash Team
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envia uma notificação para o usuário (persiste e envia via WebSocket)
     * @param user usuário destinatário
     * @param title título da notificação
     * @param message mensagem da notificação
     * @param type tipo da notificação
     */
    @Transactional
    public void sendNotification(User user, String title, String message, String type) {
        Notification notification = new Notification(user, title, message, type);
        notificationRepository.save(notification);

        NotificationDTO dto = toDTO(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId().toString(), dto);
    }

    /**
     * Busca notificações de um usuário com paginação
     * @param userId ID do usuário
     * @param pageable paginação
     * @return página de notificações
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDTO);
    }

    /**
     * Retorna a quantidade de notificações não lidas do usuário
     * @param userId ID do usuário
     * @return quantidade de notificações não lidas
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Marca todas as notificações do usuário como lidas
     * @param userId ID do usuário
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * Converte entidade Notification para NotificationDTO
     * @param notification entidade da notificação
     * @return DTO da notificação
     */
    private NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}