package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.log.gateways.LoggerPort;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Estrategia de manejo de excepciones para {@link ExpiredJwtException}.
 * Devuelve un estado HTTP 401 (Unauthorized) cuando un token JWT ha expirado.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Se ejecutará antes que el DefaultExceptionHandler
public class ExpiredJwtExceptionHandler implements ExceptionHandlerStrategy {

    private final LoggerPort logger;

    public ExpiredJwtExceptionHandler(LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return ExpiredJwtException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.UNAUTHORIZED; // 401 para token expirado
        String message = "El token de autenticación ha expirado. Por favor, inicie sesión nuevamente.";

        logger.warn("Token JWT expirado para la petición: {}", ex.getMessage());

        ErrorBody body = new ErrorBody(status.value(), status.getReasonPhrase(), message, null);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}
