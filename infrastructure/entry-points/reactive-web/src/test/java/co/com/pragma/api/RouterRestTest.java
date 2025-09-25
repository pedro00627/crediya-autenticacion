package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequestRecord;
import co.com.pragma.api.dto.response.UserResponseRecord;
import co.com.pragma.api.exception.GlobalExceptionHandler;
import co.com.pragma.api.exception.strategy.BusinessExceptionHandler;
import co.com.pragma.api.exception.strategy.DefaultExceptionHandler;
import co.com.pragma.api.exception.strategy.InvalidRequestExceptionHandler;
import co.com.pragma.api.exception.strategy.ServerWebInputExceptionHandler;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class
})
@Import({
        Router.class,
        Handler.class,
        GlobalExceptionHandler.class,
        InvalidRequestExceptionHandler.class,
        BusinessExceptionHandler.class,
        ServerWebInputExceptionHandler.class,
        DefaultExceptionHandler.class,
        RouterRestTest.TestApplication.class
})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserUseCase userUseCase;
    @MockitoBean
    private UserDTOMapper userDTOMapper;
    @MockitoBean
    private Validator validator;
    @MockitoBean
    private LoggerPort loggerPort;
    @MockitoBean
    private PasswordEncryptor passwordEncryptor;

    private UserRequestRecord userRequest;
    private User userModel;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequestRecord(
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                "1",
                50000.0,
                "testpassword"
        );

        userModel = new User(
                "dummy-id",
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0,
                "testpassword"
        );
    }

    @Test
    void saveUserShouldSucceed() {
        User savedUser = new User("gen-id-123", userModel.firstName(), userModel.lastName(), userModel.birthDate(), userModel.email(), userModel.identityDocument(), userModel.phone(), userModel.roleId(), userModel.baseSalary(), "encryptedPassword");
        UserResponseRecord response = new UserResponseRecord("gen-id-123", "John", "Doe", LocalDate.of(1990, 5, 15), "john.doe@example.com", "123456789", "3001234567", "1", 50000.0);

        when(validator.validate(any(UserRequestRecord.class))).thenReturn(Collections.emptySet());
        when(userDTOMapper.toModel(any(UserRequestRecord.class))).thenReturn(userModel);
        when(passwordEncryptor.encode(any(String.class))).thenReturn("encryptedPassword");
        when(userUseCase.saveUser(any(User.class))).thenReturn(Mono.just(savedUser));
        when(userDTOMapper.toResponse(any(User.class))).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseRecord.class)
                .isEqualTo(response);
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveUserShouldFailOnInvalidRequest() {
        UserRequestRecord invalidRequest = new UserRequestRecord(null, "Doe", LocalDate.now(), "email", "doc", "phone", "1", 1.0, "");

        ConstraintViolation<UserRequestRecord> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("firstName");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("El primer nombre no puede estar vacío");

        Set<ConstraintViolation<UserRequestRecord>> violations = Set.of(violation);
        when(validator.validate(any(UserRequestRecord.class))).thenReturn(violations);

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Validation Error")
                .jsonPath("$.messages.firstName").isEqualTo("El primer nombre no puede estar vacío");
    }

    @Test
    void saveUserShouldFailOnBusinessException() {
        final String errorMessage = "El correo electrónico 'john.doe@example.com' ya se encuentra registrado.";

        when(validator.validate(any(UserRequestRecord.class))).thenReturn(Collections.emptySet());
        when(userDTOMapper.toModel(any(UserRequestRecord.class))).thenReturn(userModel);
        when(userUseCase.saveUser(any(User.class))).thenReturn(Mono.error(new BusinessException(errorMessage)));

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Business Rule Violation")
                .jsonPath("$.message").isEqualTo(errorMessage);
    }

    @SpringBootConfiguration
    static class TestApplication {
    }
}
