package co.com.pragma.api.exception;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Excepción para solicitudes inválidas debido a errores de validación.
 * Incluye el conjunto de violaciones de restricciones.
 */
public class InvalidRequestException extends RuntimeException {
    private final Set<? extends ConstraintViolation<?>> violations;

    public InvalidRequestException(final String message, final Set<? extends ConstraintViolation<?>> violations) {
        super(message);
        this.violations = violations;
    }

    public InvalidRequestException(final Set<? extends ConstraintViolation<?>> violations) {
        this("Invalid request due to validation errors.", violations);
    }

    public InvalidRequestException(final String message, final Throwable cause, final Set<? extends ConstraintViolation<?>> violations) {
        super(message, cause);
        this.violations = violations;
    }

    public Set<? extends ConstraintViolation<?>> getViolations() {
        return this.violations;
    }
}