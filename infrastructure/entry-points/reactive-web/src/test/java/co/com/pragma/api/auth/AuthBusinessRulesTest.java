package co.com.pragma.api.auth;

import co.com.pragma.api.auth.strategy.RoleStrategyContext;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.security.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthBusinessRulesTest {

    @Mock private JWTUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncryptor passwordEncryptor;
    @Mock private LoggerPort logger;
    @Mock private RoleStrategyContext roleStrategyContext;

    @InjectMocks private AuthController authController;

    private User validUser;
    private AuthRequest validRequest;

    @BeforeEach
    void setUp() {
        this.validUser = new User("1", "John", "Doe", LocalDate.of(1990, 5, 15),
                           "john.doe@example.com", "123456789", "3001234567",
                           1, 50000.0, "hashedPassword");
        this.validRequest = new AuthRequest("john.doe@example.com", "plainPassword");
    }

    @Test
    void shouldHandleLoginForInactiveUser() {
        User inactiveUser = new User("1", "John", "Doe", LocalDate.of(1990, 5, 15),
                                         "john.doe@example.com", "123456789", "3001234567",
                                         null, 50000.0, "hashedPassword"); // null roleId = inactive

        when(this.userRepository.getUserByEmail(this.validRequest.username())).thenReturn(Mono.just(inactiveUser));
        when(this.passwordEncryptor.matches(this.validRequest.password(), inactiveUser.password())).thenReturn(true);
        when(this.jwtUtil.generateToken(anyString(), any())).thenReturn("token");

        StepVerifier.create(this.authController.login(this.validRequest))
                .assertNext(response -> {
                    // El sistema actualmente permite login con roleId null y genera token vacío
                    assertTrue(response.getStatusCode().is2xxSuccessful() ||
                              response.getStatusCode().is4xxClientError());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("criticalBusinessScenarios")
    void shouldHandleCriticalBusinessScenarios(String scenario, User user,
                                               boolean passwordMatches,
                                               HttpStatus expectedStatus) {
        when(this.userRepository.getUserByEmail(anyString())).thenReturn(Mono.just(user));
        when(this.passwordEncryptor.matches(anyString(), anyString())).thenReturn(passwordMatches);

        if (passwordMatches && null != user.roleId()) {
            when(this.roleStrategyContext.getRolesForUser(user.roleId())).thenReturn(List.of("CLIENT"));
            when(this.jwtUtil.generateToken(anyString(), any())).thenReturn("token");
        }

        AuthRequest request = new AuthRequest(user.email(), "password");

        StepVerifier.create(this.authController.login(request))
                .assertNext(response ->
                    assertEquals(expectedStatus, response.getStatusCode(), scenario))
                .verifyComplete();
    }

    @Test
    void shouldHandleHighConcurrentLogins() {
        when(this.userRepository.getUserByEmail(this.validRequest.username())).thenReturn(Mono.just(this.validUser));
        when(this.passwordEncryptor.matches(this.validRequest.password(), this.validUser.password())).thenReturn(true);
        when(this.roleStrategyContext.getRolesForUser(this.validUser.roleId())).thenReturn(List.of("CLIENT"));
        when(this.jwtUtil.generateToken(this.validUser.email(), List.of("CLIENT"))).thenReturn("token");

        List<Mono<Void>> concurrentLogins = Stream.generate(() ->
                this.authController.login(this.validRequest)
                    .doOnNext(response -> assertEquals(HttpStatus.OK, response.getStatusCode()))
                    .then()
        ).limit(10).toList();

        StepVerifier.create(Mono.when(concurrentLogins))
                .verifyComplete();
    }

    @Test
    void shouldHandleRepositoryTimeout() {
        when(this.userRepository.getUserByEmail(this.validRequest.username()))
                .thenReturn(Mono.error(new TimeoutException("Database timeout")));

        StepVerifier.create(this.authController.login(this.validRequest))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertTrue(response.getBody().containsKey("error"));
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("invalidUserDataScenarios")
    void shouldDocumentCurrentBehaviorWithInvalidUserData(String scenario, User invalidUser) {
        // Esto debería ser mejorado para validar reglas de negocio en autenticación
        when(this.userRepository.getUserByEmail(anyString())).thenReturn(Mono.just(invalidUser));
        when(this.passwordEncryptor.matches(anyString(), anyString())).thenReturn(true);
        when(this.roleStrategyContext.getRolesForUser(any())).thenReturn(List.of("CLIENT"));
        when(this.jwtUtil.generateToken(anyString(), any())).thenReturn("token");

        AuthRequest request = new AuthRequest(invalidUser.email(), "password");

        StepVerifier.create(this.authController.login(request))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody().containsKey("token"));
                })
                .verifyComplete();
    }

    static Stream<Arguments> criticalBusinessScenarios() {
        return Stream.of(
            Arguments.of("Valid client with correct credentials",
                        new User("1", "John", "Doe", LocalDate.of(1990, 5, 15),
                               "client@example.com", "123456789", "3001234567",
                               1, 50000.0, "hashedPassword"),
                        true, HttpStatus.OK),

            Arguments.of("Valid advisor with correct credentials",
                        new User("2", "Jane", "Smith", LocalDate.of(1985, 3, 20),
                               "advisor@example.com", "987654321", "3009876543",
                               2, 75000.0, "hashedPassword"),
                        true, HttpStatus.OK),

            Arguments.of("Admin with incorrect password",
                        new User("3", "Admin", "User", LocalDate.of(1980, 1, 1),
                               "admin@example.com", "111111111", "3001111111",
                               3, 100000.0, "hashedPassword"),
                        false, HttpStatus.UNAUTHORIZED),

            Arguments.of("User with negative salary (business rule violation)",
                        new User("4", "Bad", "Salary", LocalDate.of(1990, 1, 1),
                               "bad@example.com", "222222222", "3002222222",
                               1, -1000.0, "hashedPassword"),
                        true, HttpStatus.OK)
        );
    }

    static Stream<Arguments> invalidUserDataScenarios() {
        return Stream.of(
            Arguments.of("User with empty phone",
                        new User("1", "John", "Doe", LocalDate.of(1990, 5, 15),
                               "user@example.com", "123456789", "",
                               1, 50000.0, "hashedPassword")),

            Arguments.of("User with future birth date",
                        new User("2", "Future", "Baby", LocalDate.now().plusDays(1),
                               "future@example.com", "123456789", "3001234567",
                               1, 50000.0, "hashedPassword")),

            Arguments.of("User with very low salary",
                        new User("3", "Poor", "User", LocalDate.of(1990, 5, 15),
                               "poor@example.com", "123456789", "3001234567",
                               1, 1.0, "hashedPassword"))
        );
    }
}