package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequestRecord;
import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.model.constants.ErrorMessages;
import co.com.pragma.model.constants.QueryParameterConstants;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class Handler implements UserApi {
    private final LoggerPort logger;

    private final UserUseCase useCase;
    private final UserDTOMapper mapper;
    private final Validator validator;

    public Handler(final LoggerPort logger, final UserUseCase useCase, final UserDTOMapper mapper, final Validator validator) {
        this.logger = logger;
        this.useCase = useCase;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    public Mono<ServerResponse> saveUseCase(final ServerRequest serverRequest) {
        // ToDo mover logger al flujo reactivo para enmascarar el email
        this.logger.info("Recibida petición para guardar usuario en la ruta: {}", serverRequest.path());
        return serverRequest.bodyToMono(UserRequestRecord.class)
                .switchIfEmpty(Mono.error(new ServerWebInputException(ErrorMessages.INVALID_REQUEST_BODY))) // Handle empty body
                .flatMap(this::validateRequest) // Validate the DTO
                .map(this.mapper::toModel)
                .flatMap(this.useCase::saveUser)
                .flatMap(user -> ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .bodyValue(this.mapper.toResponse(user)));
    }

    @Override
    public Mono<ServerResponse> getUserByEmail(final ServerRequest serverRequest) {
        // Extract email from query parameter, handle if absent
        return serverRequest.queryParam(QueryParameterConstants.EMAIL)
                .map(email -> {
                    this.logger.info("Recibida petición para obtener usuario por email: {}", this.logger.maskEmail(email));
                    return this.useCase.getUserByEmail(email)
                            .map(this.mapper::toResponse) // Use instance method reference
                            .flatMap(response -> ServerResponse.ok()
                                    .contentType(APPLICATION_JSON)
                                    .bodyValue(response))
                            .switchIfEmpty(ServerResponse.notFound().build()); // Handle user not found
                })
                .orElse(ServerResponse.badRequest() // Handle missing email parameter
                        .contentType(APPLICATION_JSON)
                        .bodyValue("{\"error\": \"" + ErrorMessages.EMAIL_REQUIRED + "\"}"));
    }

    @Override
    public Mono<ServerResponse> getUserByEmailOrIdentityDocument(final ServerRequest serverRequest) {
        return Mono.just(serverRequest)
                .filter(req -> req.queryParam(QueryParameterConstants.EMAIL).isPresent() || req.queryParam(QueryParameterConstants.IDENTITY_DOCUMENT).isPresent())
                .flatMap(req -> {
                    final String email = req.queryParam(QueryParameterConstants.EMAIL).orElse(null);
                    final String identityDocument = req.queryParam(QueryParameterConstants.IDENTITY_DOCUMENT).orElse(null);
                    return ServerResponse.ok()
                            .contentType(APPLICATION_JSON)
                            .body(this.useCase.getUserByEmailOrIdentityDocument(email, identityDocument), User.class);
                })
                .switchIfEmpty(ServerResponse.badRequest()
                        .bodyValue("{\"error\": \"" + ErrorMessages.EMAIL_OR_DOCUMENT_REQUIRED + "\"}"));
    }

    // This method validates the UserRequestRecord DTO for the save operation.
    private Mono<UserRequestRecord> validateRequest(final UserRequestRecord request) {
        final Set<ConstraintViolation<UserRequestRecord>> violations = this.validator.validate(request);
        if (violations.isEmpty()) {
            return Mono.just(request);
        }
        this.logger.info("La validación de la petición falló. Violaciones: {}", violations);
        return Mono.error(new InvalidRequestException("Invalid request due to validation errors.", violations));
    }
}
