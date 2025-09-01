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
public class Handler implements UserApi {
    private static final Logger log = LogManager.getLogger(Handler.class);

    private final UserUseCase useCase;
    private final UserDTOMapper mapper;
    private final Validator validator;

    public Handler(UserUseCase useCase, UserDTOMapper mapper, Validator validator) {
        this.useCase = useCase;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    public Mono<ServerResponse> saveUseCase(ServerRequest serverRequest) {
        log.info("Recibida petición para guardar usuario en la ruta: {}", serverRequest.path());
        return serverRequest.bodyToMono(UserRequestRecord.class)
                .switchIfEmpty(Mono.error(new ServerWebInputException("El cuerpo de la petición no puede estar vacío."))) // Handle empty body
                .flatMap(this::validateRequest) // Validate the DTO
                .map(mapper::toModel)
                .flatMap(useCase::saveUser)
                .flatMap(user -> ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .bodyValue(mapper.toResponse(user)));
    }

    @Override
    public Mono<ServerResponse> getUserByEmail(ServerRequest serverRequest) {
        // Extract email from query parameter, handle if absent
        return serverRequest.queryParam("email")
                .map(email -> {
                    log.info("Recibida petición para obtener usuario por email: {}", email);
                    return useCase.getUserByEmail(email)
                            .map(mapper::toResponse) // Use instance method reference
                            .flatMap(response -> ServerResponse.ok()
                                    .contentType(APPLICATION_JSON)
                                    .bodyValue(response))
                            .switchIfEmpty(ServerResponse.notFound().build()); // Handle user not found
                })
                .orElse(ServerResponse.badRequest() // Handle missing email parameter
                        .contentType(APPLICATION_JSON)
                        .bodyValue("{\"error\": \"El parámetro 'email' es requerido.\"}"));
    }

    // This method validates the UserRequestRecord DTO for the save operation.
    private Mono<UserRequestRecord> validateRequest(UserRequestRecord request) {
        Set<ConstraintViolation<UserRequestRecord>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return Mono.just(request);
        }
        log.warn("La validación de la petición falló. Violaciones: {}", violations);
        return Mono.error(new InvalidRequestException("Invalid request due to validation errors.", violations));
    }
}
