package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface ExceptionHandlerStrategy {
    boolean supports(Class<? extends Throwable> type);

    Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange);
}