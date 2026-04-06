package com.witalo.digitalbank.user.event;

import com.witalo.digitalbank.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Evento disparado quando um novo usuário é criado no sistema.
 * Utilizado para executar ações assíncronas como envio de e-mail de boas-vindas,
 * criação de conta bancária padrão, etc.
 *
 * @author BankDash Team
 */
@Getter
@RequiredArgsConstructor
public class UserCreatedEvent {

    private final User user;

}