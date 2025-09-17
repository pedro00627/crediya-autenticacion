package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Estrategia de manejo de excepciones para {@link AccessDeniedException}.
 * Devuelve un estado HTTP 403 (Forbidden) cuando un usuario autenticado
 * intenta acceder a un recurso sin los permisos adecuados.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Se ejecutará antes que el DefaultExceptionHandler
public class AccessDeniedExceptionHandler implements ExceptionHandlerStrategy {

    private final LoggerPort logger;

    public AccessDeniedExceptionHandler(LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return AccessDeniedException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.FORBIDDEN; // 403 para acceso denegado
        String message = "Acceso denegado. No tiene los permisos necesarios para realizar esta acción.";

        logger.warn("Acceso denegado para la petición: {}", ex.getMessage());

        ErrorBody body = new ErrorBody(status.value(), status.getReasonPhrase(), message, null);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}
