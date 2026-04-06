package com.witalo.digitalbank.notification.repository;

import com.witalo.digitalbank.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository para operações de persistência de notificações.
 *
 * @author BankDash Team
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Busca notificações de um usuário ordenadas por data decrescente
     * @param userId ID do usuário
     * @param pageable paginação
     * @return página de notificações
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Conta notificações não lidas de um usuário
     * @param userId ID do usuário
     * @return quantidade de notificações não lidas
     */
    long countByUserIdAndReadFalse(UUID userId);

    /**
     * Marca todas as notificações de um usuário como lidas
     * @param userId ID do usuário
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    void markAllAsRead(UUID userId);

}