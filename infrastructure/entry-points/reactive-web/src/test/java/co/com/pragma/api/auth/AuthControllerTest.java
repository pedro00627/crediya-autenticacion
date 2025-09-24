package co.com.pragma.api.auth;

import co.com.pragma.api.auth.strategy.RoleStrategyContext;
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
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncryptor passwordEncryptor;

    @Mock
    private LoggerPort logger;

    @Mock
    private RoleStrategyContext roleStrategyContext;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private AuthRequest authRequest;

    static Stream<Arguments> roleTestCases() {
        return Stream.of(
                Arguments.of(1, List.of("CLIENT"), "Client role should map correctly"),
                Arguments.of(2, List.of("ADVISOR"), "Advisor role should map correctly"),
                Arguments.of(3, List.of("ADMIN"), "Admin role should map correctly"),
                Arguments.of(99, List.of(), "Unknown role should return empty list")
        );
    }

    static Stream<Arguments> authRequestTestCases() {
        return Stream.of(
                Arguments.of("valid@example.com", "validPassword", true, true, HttpStatus.OK, "Valid credentials should succeed"),
                Arguments.of("valid@example.com", "invalidPassword", true, false, HttpStatus.UNAUTHORIZED, "Invalid password should fail"),
                Arguments.of("nonexistent@example.com", "anyPassword", false, false, HttpStatus.UNAUTHORIZED, "Non-existent user should fail"),
                Arguments.of("admin@example.com", "adminPassword", true, true, HttpStatus.OK, "Admin credentials should succeed"),
                Arguments.of("", "password", false, false, HttpStatus.UNAUTHORIZED, "Empty username should fail")
        );
    }

    @BeforeEach
    void setUp() {
        testUser = new User(
                "1",
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0,
                "hashedPassword"
        );

        authRequest = new AuthRequest("john.doe@example.com", "plainPassword");
    }

    @Test
    void loginShouldSucceedWithValidCredentials() {
        // Arrange
        final String expectedToken = "jwt.token.here";
        List<String> roles = List.of("CLIENT");

        when(userRepository.getUserByEmail(authRequest.username())).thenReturn(Mono.just(testUser));
        when(passwordEncryptor.matches(authRequest.password(), testUser.password())).thenReturn(true);
        when(roleStrategyContext.getRolesForUser(testUser.roleId())).thenReturn(roles);
        when(jwtUtil.generateToken(testUser.email(), roles)).thenReturn(expectedToken);

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = authController.login(authRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(expectedToken, response.getBody().get("token"));
                })
                .verifyComplete();

        verify(userRepository).getUserByEmail(authRequest.username());
        verify(passwordEncryptor).matches(authRequest.password(), testUser.password());
        verify(roleStrategyContext).getRolesForUser(testUser.roleId());
        verify(jwtUtil).generateToken(testUser.email(), roles);
    }

    @Test
    void loginShouldFailWithInvalidPassword() {
        // Arrange
        when(userRepository.getUserByEmail(authRequest.username())).thenReturn(Mono.just(testUser));
        when(passwordEncryptor.matches(authRequest.password(), testUser.password())).thenReturn(false);

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = authController.login(authRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                    assertTrue(response.getBody().containsKey("error"));
                })
                .verifyComplete();
    }

    @Test
    void loginShouldFailWithUserNotFound() {
        // Arrange
        when(userRepository.getUserByEmail(authRequest.username())).thenReturn(Mono.empty());

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = authController.login(authRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                    assertTrue(response.getBody().containsKey("error"));
                })
                .verifyComplete();
    }

    @Test
    void loginShouldHandleRepositoryError() {
        // Arrange
        RuntimeException repositoryError = new RuntimeException("Database connection failed");
        when(userRepository.getUserByEmail(authRequest.username())).thenReturn(Mono.error(repositoryError));

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = authController.login(authRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertTrue(response.getBody().containsKey("error"));
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("roleTestCases")
    void loginShouldHandleDifferentRoles(Integer roleId, List<String> expectedRoles, String scenario) {
        // Arrange
        User userWithRole = new User("1", "Test", "User", LocalDate.now(),
                "test@example.com", "123456789", "3001234567",
                roleId, 50000.0, "hashedPassword");
        AuthRequest request = new AuthRequest("test@example.com", "password");
        final String expectedToken = "jwt.token.here";

        when(userRepository.getUserByEmail(request.username())).thenReturn(Mono.just(userWithRole));
        when(passwordEncryptor.matches(request.password(), userWithRole.password())).thenReturn(true);
        when(roleStrategyContext.getRolesForUser(roleId)).thenReturn(expectedRoles);
        when(jwtUtil.generateToken(userWithRole.email(), expectedRoles)).thenReturn(expectedToken);

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = authController.login(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode(), scenario);
                    assertEquals(expectedToken, response.getBody().get("token"), scenario);
                })
                .verifyComplete();

        verify(roleStrategyContext).getRolesForUser(roleId);
    }

    @Test
    void loginShouldHandleNullRoleId() {
        // Arrange
        User userWithNullRole = new User("1", "Test", "User", LocalDate.now(),
                "test@example.com", "123456789", "3001234567",
                null, 50000.0, "hashedPassword");
        AuthRequest request = new AuthRequest("test@example.com", "password");
        final String expectedToken = "jwt.token.here";
        List<String> emptyRoles = List.of();

        when(userRepository.getUserByEmail(request.username())).thenReturn(Mono.just(userWithNullRole));
        when(passwordEncryptor.matches(request.password(), userWithNullRole.password())).thenReturn(true);
        when(jwtUtil.generateToken(userWithNullRole.email(), emptyRoles)).thenReturn(expectedToken);

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = authController.login(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(expectedToken, response.getBody().get("token"));
                })
                .verifyComplete();

        // Note: roleStrategyContext.getRolesForUser(null) is NOT called because
        // the controller handles null roleId directly by returning empty list
        verify(userRepository).getUserByEmail(request.username());
        verify(passwordEncryptor).matches(request.password(), userWithNullRole.password());
        verify(jwtUtil).generateToken(userWithNullRole.email(), emptyRoles);
    }

    @ParameterizedTest
    @MethodSource("authRequestTestCases")
    void loginShouldHandleDifferentAuthRequests(String username, String password,
                                                boolean userExists, boolean passwordMatches,
                                                HttpStatus expectedStatus, String scenario) {
        // Arrange
        AuthRequest request = new AuthRequest(username, password);

        if (userExists) {
            when(userRepository.getUserByEmail(username)).thenReturn(Mono.just(testUser));
            when(passwordEncryptor.matches(password, testUser.password())).thenReturn(passwordMatches);
            if (passwordMatches) {
                when(roleStrategyContext.getRolesForUser(any())).thenReturn(List.of("CLIENT"));
                when(jwtUtil.generateToken(anyString(), any())).thenReturn("token");
            }
        } else {
            when(userRepository.getUserByEmail(username)).thenReturn(Mono.empty());
        }

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = authController.login(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(expectedStatus, response.getStatusCode(), scenario))
                .verifyComplete();
    }
}