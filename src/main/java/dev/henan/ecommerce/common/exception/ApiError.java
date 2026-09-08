package dev.henan.ecommerce.common.exception;

import java.time.Instant;
import java.util.List;

/**
 * Corpo padronizado de erro devolvido pela API.
 *
 * @param timestamp momento em que o erro foi gerado
 * @param status    codigo HTTP
 * @param error     nome curto do status HTTP
 * @param message   descricao legivel do que aconteceu
 * @param path      URI que originou o erro
 * @param fields    erros de validacao por campo (vazio quando nao se aplica)
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fields
) {

    public record FieldError(String field, String message) {
    }

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path, List<FieldError> fields) {
        return new ApiError(Instant.now(), status, error, message, path, fields);
    }
}
