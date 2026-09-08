package dev.henan.ecommerce.common.exception;

/**
 * Recurso inexistente. Mapeada para HTTP 404 pelo GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s nao encontrado(a) para o identificador %s".formatted(resource, id));
    }
}
