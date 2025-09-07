package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.log.gateways.LoggerPort;
import jakarta.validation.ConstraintViolation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    public InvalidRequestExceptionHandler(LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return InvalidRequestException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        InvalidRequestException exception = (InvalidRequestException) ex;

        Map<String, String> messages = exception.getViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        logger.info("Error de validación en la petición [{}]: {}", exchange.getRequest().getPath(), messages);

        ErrorBody body = new ErrorBody(status.value(), "Validation Error", null, messages);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}