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
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {})
@Import({
        Router.class,
        Handler.class,
        GlobalExceptionHandler.class,
        InvalidRequestExceptionHandler.class,
        BusinessExceptionHandler.class,
        ServerWebInputExceptionHandler.class,
        DefaultExceptionHandler.class,
        RouterRestTest.TestConfig.class
})
class RouterRestTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserUseCase userUseCase() {
            return Mockito.mock(UserUseCase.class);
        }

        @Bean
        public UserDTOMapper userDTOMapper() {
            return Mockito.mock(UserDTOMapper.class);
        }

        @Bean
        public Validator validator() {
            return Mockito.mock(Validator.class);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private UserDTOMapper userDTOMapper;

    @Autowired
    private Validator validator;

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
                50000.0
        );

        userModel = new User(
                null, // ID es nulo antes de guardar
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0
        );
    }

    @Test
    void saveUserShouldSucceed() {
        // Arrange
        User savedUser = new User("gen-id-123", userModel.firstName(), userModel.lastName(), userModel.birthDate(), userModel.email(), userModel.identityDocument(), userModel.phone(), userModel.roleId(), userModel.baseSalary());
        UserResponseRecord response = new UserResponseRecord("gen-id-123", "John", "Doe", LocalDate.of(1990, 5, 15), "john.doe@example.com", "123456789", "3001234567", "1", 50000.0);

        // Mocking de la cadena de ejecución exitosa
        when(validator.validate(any(UserRequestRecord.class))).thenReturn(Collections.emptySet());
        when(userDTOMapper.toModel(any(UserRequestRecord.class))).thenReturn(userModel);
        when(userUseCase.saveUser(any(User.class))).thenReturn(Mono.just(savedUser));
        when(userDTOMapper.toResponse(any(User.class))).thenReturn(response);

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseRecord.class)
                .isEqualTo(response);
    }

    @Test
    void saveUserShouldFailOnInvalidRequest() {
        // Arrange: Simular una violación de validación
        UserRequestRecord invalidRequest = new UserRequestRecord(null, "Doe", LocalDate.now(), "email", "doc", "phone", "1", 1.0);

        ConstraintViolation<UserRequestRecord> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("firstName");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("El primer nombre no puede estar vacío");

        Set<ConstraintViolation<UserRequestRecord>> violations = Set.of(violation);
        when(validator.validate(any(UserRequestRecord.class))).thenReturn(violations);

        // Act & Assert
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
        // Arrange: Simular una excepción de negocio (ej. email duplicado)
        String errorMessage = "El correo electrónico 'john.doe@example.com' ya se encuentra registrado.";

        when(validator.validate(any(UserRequestRecord.class))).thenReturn(Collections.emptySet());
        when(userDTOMapper.toModel(any(UserRequestRecord.class))).thenReturn(userModel);
        when(userUseCase.saveUser(any(User.class))).thenReturn(Mono.error(new BusinessException(errorMessage)));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT) // 409
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Business Rule Violation")
                .jsonPath("$.message").isEqualTo(errorMessage);
    }
}
