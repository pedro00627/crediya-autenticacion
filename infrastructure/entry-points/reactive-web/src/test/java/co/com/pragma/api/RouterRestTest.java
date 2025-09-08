package co.com.pragma.api;
/*
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
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.api.utils.JWTUtil;
import co.com.pragma.api.config.JWTConfig;
import co.com.pragma.api.auth.AuthController;
import co.com.pragma.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {AuthController.class},
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {GlobalExceptionHandler.class, JWTUtil.class, JWTConfig.class}),
        excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class},
        properties = "spring.security.filter.enabled=false" // Deshabilita los filtros de seguridad
)
@Import({
        GlobalExceptionHandler.class,
        InvalidRequestExceptionHandler.class,
        BusinessExceptionHandler.class,
        ServerWebInputExceptionHandler.class,
        DefaultExceptionHandler.class,
        PasswordEncryptor.class
})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    // Reemplazamos @Autowired con @MockitoBean para los servicios mockeados
    @MockitoBean
    private UserUseCase userUseCase;
    @MockitoBean
    private UserDTOMapper userDTOMapper;
    @MockitoBean
    private Validator validator;
    @MockitoBean
    private LoggerPort loggerPort;
    @MockitoBean
    private JWTUtil jwtUtil;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncryptor passwordEncryptor;
    @MockitoBean
    private co.com.pragma.api.security.JWTAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private co.com.pragma.api.security.UserAuthorizationManager userAuthorizationManager;

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
                "dummy-id", // Added a dummy ID here
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
        // Arrange
        User savedUser = new User("gen-id-123", userModel.firstName(), userModel.lastName(), userModel.birthDate(), userModel.email(), userModel.identityDocument(), userModel.phone(), userModel.roleId(), userModel.baseSalary(),"encryptedPassword");
        UserResponseRecord response = new UserResponseRecord("gen-id-123", "John", "Doe", LocalDate.of(1990, 5, 15), "john.doe@example.com", "123456789", "3001234567", "1", 50000.0);

        // Mocking de la cadena de ejecución exitosa
        when(validator.validate(any(UserRequestRecord.class))).thenReturn(Collections.emptySet());
        when(userDTOMapper.toModel(any(UserRequestRecord.class))).thenReturn(userModel);
        when(passwordEncryptor.encode(any(String.class))).thenReturn("encryptedPassword");
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
        UserRequestRecord invalidRequest = new UserRequestRecord(null, "Doe", LocalDate.now(), "email", "doc", "phone", "1", 1.0,"");

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
*/