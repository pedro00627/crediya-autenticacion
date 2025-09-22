package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.log.gateways.LoggerPort;
import jakarta.validation.ConstraintViolation;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(0) // Prioridad más alta para excepciones específicas
public class InvalidRequestExceptionHandler implements ExceptionHandlerStrategy {

    private final LoggerPort logger;

    public InvalidRequestExceptionHandler(final LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(final Class<? extends Throwable> type) {
        return InvalidRequestException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(final Throwable ex, final ServerWebExchange exchange) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final InvalidRequestException exception = (InvalidRequestException) ex;

        final Map<String, String> messages = exception.getViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        this.logger.info("Error de validación en la petición [{}]: {}", exchange.getRequest().getPath(), messages);

        final ErrorBody body = new ErrorBody(status.value(), "Validation Error", null, messages);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}