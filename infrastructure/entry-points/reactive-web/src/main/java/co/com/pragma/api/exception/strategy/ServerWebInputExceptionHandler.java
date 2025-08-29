package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class ServerWebInputExceptionHandler implements ExceptionHandlerStrategy {

    private static final Logger log = LogManager.getLogger(ServerWebInputExceptionHandler.class);

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return ServerWebInputException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ServerWebInputException exception = (ServerWebInputException) ex;

        log.warn("Error de entrada en la petición [{}]: {}", exchange.getRequest().getPath(), exception.getReason());

        String reason = "El cuerpo de la petición tiene un formato inválido.";
        if (ex.getMessage().contains("LocalDate")) {
            reason = "El formato de fecha es inválido. Por favor, use el formato 'YYYY-MM-DD'.";
        }

        ErrorBody body = new ErrorBody(status.value(), "Invalid Input", reason, null);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}