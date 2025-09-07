package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.LOWEST_PRECEDENCE) // Se ejecutará al final si ninguna otra estrategia coincide
public class DefaultExceptionHandler implements ExceptionHandlerStrategy {

    private final LoggerPort logger;

    public DefaultExceptionHandler(LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return true;
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logger.error("Ocurrió una excepción no controlada para la petición", ex);

        String message = "Ocurrió un error inesperado. Por favor, contacte al soporte.";
        ErrorBody body = new ErrorBody(status.value(), "Internal Server Error", message, null);

        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}