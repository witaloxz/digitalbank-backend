package com.witalo.digitalbank.common.exception;

import com.witalo.digitalbank.account.exception.*;
import com.witalo.digitalbank.auth.exception.InvalidCredentialsException;
import com.witalo.digitalbank.auth.exception.UserNotFoundException;
import com.witalo.digitalbank.card.exception.CardLimitExceededException;
import com.witalo.digitalbank.card.exception.CardNotFoundException;
import com.witalo.digitalbank.card.exception.InvalidCreditLimitException;
import com.witalo.digitalbank.transaction.exception.TransactionBusinessException;
import com.witalo.digitalbank.user.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

/**
 * Manipulador global de exceções da aplicação.
 * Centraliza o tratamento de erros e padroniza as respostas da API.
 *
 * @author BankDash Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================
    // EXCEPTIONS GERAIS
    // ============================

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.NOT_FOUND, "Endpoint Not Found", "Check the requested URL", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.BAD_REQUEST, "Invalid Parameter", "The provided ID is invalid", request);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingPathVariable(MissingPathVariableException ex, HttpServletRequest request) {
        String message = "The parameter '" + ex.getVariableName() + "' is required";
        return buildApiError(HttpStatus.BAD_REQUEST, "Missing Path Variable", message, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(Exception ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", request);
    }

    // ============================
    // VALIDAÇÃO
    // ============================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Invalid data");
        return buildApiError(HttpStatus.BAD_REQUEST, "Validation Error", message, request);
    }

    // ============================
    // EXCEPTIONS DE USUÁRIO
    // ============================

    @ExceptionHandler({UserNotFoundException.class, EmailNotFoundException.class, CpfNotFoundException.class, UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, CpfAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAlreadyExists(RuntimeException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.CONFLICT, "Resource Already Exists", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleInvalidCredentials(RuntimeException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.UNAUTHORIZED, "Invalid Credentials", ex.getMessage(), request);
    }

    // ============================
    // EXCEPTIONS DE CONTA
    // ============================

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.NOT_FOUND, "Account Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler({AccountAlreadyExistsException.class, AccountNumberAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAccountAlreadyExists(RuntimeException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.CONFLICT, "Account Already Exists", ex.getMessage(), request);
    }

    @ExceptionHandler({
            AccountAlreadyActiveException.class,
            AccountAlreadyInactiveException.class,
            AccountInactiveException.class,
            InvalidAccountStatusException.class,
            AccountStatusAlreadyException.class,
            InvalidAmountException.class,
            InsufficientBalanceException.class,
            SameAccountTransferException.class,
            AccountBusinessException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleAccountBusinessRules(RuntimeException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.BAD_REQUEST, "Business Rule Violation", ex.getMessage(), request);
    }

    // ============================
    // EXCEPTIONS DE TRANSAÇÃO
    // ============================

    @ExceptionHandler(TransactionBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTransactionBusiness(TransactionBusinessException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.BAD_REQUEST, "Transaction Error", ex.getMessage(), request);
    }

    // ============================
    // EXCEPTIONS DE CARTÃO
    // ============================

    @ExceptionHandler(CardNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleCardNotFound(CardNotFoundException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.NOT_FOUND, "Card Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(CardLimitExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleCardLimitExceeded(CardLimitExceededException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.CONFLICT, "Card Already Exists", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCreditLimitException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidCreditLimit(InvalidCreditLimitException ex, HttpServletRequest request) {
        return buildApiError(HttpStatus.BAD_REQUEST, "Invalid Credit Limit", ex.getMessage(), request);
    }

    // ============================
    // EXCEPTIONS DE BANCO DE DADOS
    // ============================

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Data integrity violation. Please check unique fields.";
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null && cause.getMessage() != null) {
            if (cause.getMessage().contains("users_email_key")) {
                message = "Email already registered";
            } else if (cause.getMessage().contains("users_cpf_key")) {
                message = "CPF already registered";
            } else {
                message = cause.getMessage();
            }
        }
        return buildApiError(HttpStatus.CONFLICT, "Data Integrity Violation", message, request);
    }

    // ============================
    // MÉTODO AUXILIAR
    // ============================

    private ApiError buildApiError(HttpStatus status, String error, String message, HttpServletRequest request) {
        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
    }
}