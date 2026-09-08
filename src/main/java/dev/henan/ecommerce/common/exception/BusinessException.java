package dev.henan.ecommerce.common.exception;

/**
 * Violacao de regra de negocio do dominio (estoque insuficiente, transicao de
 * status invalida, etc). Mapeada para HTTP 422 pelo GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
