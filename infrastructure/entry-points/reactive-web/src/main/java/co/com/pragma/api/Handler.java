package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequestRecord;
import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class Handler {
    private static final Logger log = LogManager.getLogger(Handler.class);

    private final UserUseCase useCase;
    private final UserDTOMapper mapper;
    private final Validator validator;

    public Handler(UserUseCase useCase, UserDTOMapper mapper, Validator validator) {
        this.useCase = useCase;
        this.mapper = mapper;
        this.validator = validator;
    }

    public Mono<ServerResponse> saveUseCase(ServerRequest serverRequest) {
        log.info("Recibida petición para guardar usuario en la ruta: {}", serverRequest.path());
        return serverRequest.bodyToMono(UserRequestRecord.class)
                .switchIfEmpty(Mono.error(new ServerWebInputException("El cuerpo de la petición no puede estar vacío.")))
                .flatMap(this::validateRequest)
                .map(mapper::toModel)
                .flatMap(useCase::saveUser)
                .doOnSuccess(user -> log.info("Usuario guardado exitosamente con ID: {}", user.id()))
                .flatMap(user -> ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .bodyValue(mapper.toResponse(user)));
    }

    private Mono<UserRequestRecord> validateRequest(UserRequestRecord request) {
        log.debug("Validando la estructura de la petición para el email: {}", request.email());
        Set<ConstraintViolation<UserRequestRecord>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return Mono.just(request);
        }
        log.warn("La validación de la petición falló. Violaciones: {}", violations);
        // Lanza una excepción personalizada que será capturada por el GlobalExceptionHandler
        return Mono.error(new InvalidRequestException(violations));
    }
}
