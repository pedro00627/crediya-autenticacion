package co.com.pragma.api.exception;

import co.com.pragma.model.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(-2) // Se asegura de que se ejecute antes que el manejador de errores por defecto de Spring
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        HttpStatus status;

        if (ex instanceof InvalidRequestException) {
            // Captura errores de validación de la petición
            status = HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", "Validation Error");
            Map<String, String> messages = ((InvalidRequestException) ex).getViolations().stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    ));
            errorResponse.put("messages", messages);
            log.warn("Error de validación en la petición [{}]: {}", exchange.getRequest().getPath(), messages);
        } else if (ex instanceof ServerWebInputException) {
            // Captura otros errores de entrada de WebFlux (ej. cuerpo malformado)
            status = HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", "Invalid Input");
            errorResponse.put("messages", ((ServerWebInputException) ex).getReason());
            String reason = "El cuerpo de la petición tiene un formato inválido.";
            if (ex.getMessage().contains("LocalDate")) {
                reason = "El formato de fecha es inválido. Por favor, use el formato 'YYYY-MM-DD'.";
            }
            errorResponse.put("message", reason);
        } else if (ex instanceof BusinessException) {
            // Captura errores de lógica de negocio
            status = HttpStatus.CONFLICT; // 409 Conflict es adecuado para violaciones de reglas de negocio
            errorResponse.put("status", status.value());
            errorResponse.put("error", "Business Rule Violation");
            errorResponse.put("message", ex.getMessage());
            log.warn("Violación de regla de negocio para la petición [{}]: {}", exchange.getRequest().getPath(), ex.getMessage());
        } else {
            // Captura cualquier otra excepción no controlada (errores internos)
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse.put("status", status.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Ocurrió un error inesperado. Por favor, contacte al soporte.");
            log.error("Ocurrió una excepción no controlada para la petición [{}]", exchange.getRequest().getPath(), ex);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            // Serializa el mapa de error a JSON y lo escribe en la respuesta
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error escribiendo la respuesta de error en formato JSON", e);
            return Mono.error(e);
        }
    }
}