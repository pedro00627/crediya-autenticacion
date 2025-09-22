package co.com.pragma.model.exception;

/**
 * Clase de excepción personalizada para reglas de negocio.
 */
public class BusinessException extends RuntimeException {
    /**
     * @param message Mensaje de error personalizado.
     */
    public BusinessException(final String message) {
        super(message);
    }
}