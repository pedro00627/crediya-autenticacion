package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.constants.ErrorMessages;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class ServerWebInputExceptionHandler implements ExceptionHandlerStrategy {

    private final LoggerPort logger;

    public ServerWebInputExceptionHandler(final LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(final Class<? extends Throwable> type) {
        return ServerWebInputException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(final Throwable ex, final ServerWebExchange exchange) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final ServerWebInputException exception = (ServerWebInputException) ex;

        this.logger.info("Error de entrada en la petición", exception.getReason());

        String reason = ErrorMessages.INVALID_REQUEST_FORMAT;
        if (ex.getMessage().contains("LocalDate")) {
            reason = ErrorMessages.INVALID_DATE_FORMAT_MESSAGE;
        }

        final ErrorBody body = new ErrorBody(status.value(), ErrorMessages.INVALID_INPUT_CATEGORY, reason, null);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}