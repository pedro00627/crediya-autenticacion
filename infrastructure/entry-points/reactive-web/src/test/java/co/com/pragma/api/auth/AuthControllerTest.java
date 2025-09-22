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

    @BeforeEach
    void setUp() {
        this.testUser = new User(
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

        this.authRequest = new AuthRequest("john.doe@example.com", "plainPassword");
    }

    @Test
    void loginShouldSucceedWithValidCredentials() {
        // Arrange
        final String expectedToken = "jwt.token.here";
        final List<String> roles = List.of("CLIENT");

        when(this.userRepository.getUserByEmail(this.authRequest.username())).thenReturn(Mono.just(this.testUser));
        when(this.passwordEncryptor.matches(this.authRequest.password(), this.testUser.password())).thenReturn(true);
        when(this.roleStrategyContext.getRolesForUser(this.testUser.roleId())).thenReturn(roles);
        when(this.jwtUtil.generateToken(this.testUser.email(), roles)).thenReturn(expectedToken);

        // Act
        final Mono<ResponseEntity<Map<String, String>>> result = this.authController.login(this.authRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(expectedToken, response.getBody().get("token"));
                })
                .verifyComplete();

        verify(this.userRepository).getUserByEmail(this.authRequest.username());
        verify(this.passwordEncryptor).matches(this.authRequest.password(), this.testUser.password());
        verify(this.roleStrategyContext).getRolesForUser(this.testUser.roleId());
        verify(this.jwtUtil).generateToken(this.testUser.email(), roles);
    }

    @Test
    void loginShouldFailWithInvalidPassword() {
        // Arrange
        when(this.userRepository.getUserByEmail(this.authRequest.username())).thenReturn(Mono.just(this.testUser));
        when(this.passwordEncryptor.matches(this.authRequest.password(), this.testUser.password())).thenReturn(false);

        // Act
        final Mono<ResponseEntity<Map<String, String>>> result = this.authController.login(this.authRequest);

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
        when(this.userRepository.getUserByEmail(this.authRequest.username())).thenReturn(Mono.empty());

        // Act
        final Mono<ResponseEntity<Map<String, String>>> result = this.authController.login(this.authRequest);

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
        final RuntimeException repositoryError = new RuntimeException("Database connection failed");
        when(this.userRepository.getUserByEmail(this.authRequest.username())).thenReturn(Mono.error(repositoryError));

        // Act
        final Mono<ResponseEntity<Map<String, String>>> result = this.authController.login(this.authRequest);

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
    void loginShouldHandleDifferentRoles(final Integer roleId, final List<String> expectedRoles, final String scenario) {
        // Arrange
        final User userWithRole = new User("1", "Test", "User", LocalDate.now(),
                                   "test@example.com", "123456789", "3001234567",
                                   roleId, 50000.0, "hashedPassword");
        final AuthRequest request = new AuthRequest("test@example.com", "password");
        final String expectedToken = "jwt.token.here";

        when(this.userRepository.getUserByEmail(request.username())).thenReturn(Mono.just(userWithRole));
        when(this.passwordEncryptor.matches(request.password(), userWithRole.password())).thenReturn(true);
        when(this.roleStrategyContext.getRolesForUser(roleId)).thenReturn(expectedRoles);
        when(this.jwtUtil.generateToken(userWithRole.email(), expectedRoles)).thenReturn(expectedToken);

        // Act
        final Mono<ResponseEntity<Map<String, String>>> result = this.authController.login(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode(), scenario);
                    assertEquals(expectedToken, response.getBody().get("token"), scenario);
                })
                .verifyComplete();

        verify(this.roleStrategyContext).getRolesForUser(roleId);
    }

    @Test
    void loginShouldHandleNullRoleId() {
        // Arrange
        final User userWithNullRole = new User("1", "Test", "User", LocalDate.now(),
                                       "test@example.com", "123456789", "3001234567",
                                       null, 50000.0, "hashedPassword");
        final AuthRequest request = new AuthRequest("test@example.com", "password");
        final String expectedToken = "jwt.token.here";
        final List<String> emptyRoles = List.of();

        when(this.userRepository.getUserByEmail(request.username())).thenReturn(Mono.just(userWithNullRole));
        when(this.passwordEncryptor.matches(request.password(), userWithNullRole.password())).thenReturn(true);
        when(this.jwtUtil.generateToken(userWithNullRole.email(), emptyRoles)).thenReturn(expectedToken);

        // Act
        final Mono<ResponseEntity<Map<String, String>>> result = this.authController.login(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(expectedToken, response.getBody().get("token"));
                })
                .verifyComplete();

        // Note: roleStrategyContext.getRolesForUser(null) is NOT called because
        // the controller handles null roleId directly by returning empty list
        verify(this.userRepository).getUserByEmail(request.username());
        verify(this.passwordEncryptor).matches(request.password(), userWithNullRole.password());
        verify(this.jwtUtil).generateToken(userWithNullRole.email(), emptyRoles);
    }

    @ParameterizedTest
    @MethodSource("authRequestTestCases")
    void loginShouldHandleDifferentAuthRequests(final String username, final String password,
                                                final boolean userExists, final boolean passwordMatches,
                                                final HttpStatus expectedStatus, final String scenario) {
        // Arrange
        final AuthRequest request = new AuthRequest(username, password);

        if (userExists) {
            when(this.userRepository.getUserByEmail(username)).thenReturn(Mono.just(this.testUser));
            when(this.passwordEncryptor.matches(password, this.testUser.password())).thenReturn(passwordMatches);
            if (passwordMatches) {
                when(this.roleStrategyContext.getRolesForUser(any())).thenReturn(List.of("CLIENT"));
                when(this.jwtUtil.generateToken(anyString(), any())).thenReturn("token");
            }
        } else {
            when(this.userRepository.getUserByEmail(username)).thenReturn(Mono.empty());
        }

        // Act
        final Mono<ResponseEntity<Map<String, String>>> result = this.authController.login(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(expectedStatus, response.getStatusCode(), scenario))
                .verifyComplete();
    }

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
}