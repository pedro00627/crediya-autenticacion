package co.com.pragma.api.exception;

import co.com.pragma.api.exception.strategy.ExceptionHandlerStrategy;
import co.com.pragma.model.log.gateways.LoggerPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(-2) // Se asegura de que se ejecute antes que el manejador de errores por defecto de Spring
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final LoggerPort logger;
    private final ObjectMapper objectMapper;
    private final List<ExceptionHandlerStrategy> strategies;

    public GlobalExceptionHandler(final LoggerPort logger, final ObjectMapper objectMapper, final List<ExceptionHandlerStrategy> strategies) {
        this.logger = logger;
        this.objectMapper = objectMapper;
        this.strategies = strategies;
    }

    @Override
    public Mono<Void> handle(final ServerWebExchange exchange, final Throwable ex) {
        // Encuentra la primera estrategia que soporta el tipo de excepción
        return this.strategies.stream()
                .filter(strategy -> strategy.supports(ex.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró un manejador de excepciones por defecto."))
                .handle(ex, exchange)
                .flatMap(errorWrapper -> {
                    exchange.getResponse().setStatusCode(errorWrapper.httpStatus());
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    try {
                        final byte[] bytes = this.objectMapper.writeValueAsBytes(errorWrapper.body());
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                    } catch (final JsonProcessingException e) {
                        this.logger.error("Error escribiendo la respuesta de error en formato JSON", e);
                        return Mono.error(e);
                    }
                });
    }
}