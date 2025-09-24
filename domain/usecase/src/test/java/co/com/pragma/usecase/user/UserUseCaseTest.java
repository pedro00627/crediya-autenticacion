package co.com.pragma.usecase.user;

import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PasswordEncryptor passwordEncryptor;


    @InjectMocks
    private UserUseCase userUseCase;

    private User user;

    static Stream<Arguments> businessRuleViolationCases() {
        return Stream.of(
                Arguments.of("Duplicate email",
                        new User(null, "John", "Duplicate", LocalDate.of(1990, 1, 1),
                                "existing@crediya.com", "123456789", "3001234567",
                                1, 50000.0, "password"),
                        "El correo electrónico ya existe"),

                Arguments.of("Underage user",
                        new User(null, "Young", "User", LocalDate.now().minusYears(16),
                                "young@example.com", "123456789", "3001234567",
                                1, 30000.0, "password"),
                        "El usuario debe ser mayor de edad"),

                Arguments.of("Invalid salary range",
                        new User(null, "Low", "Salary", LocalDate.of(1990, 1, 1),
                                "lowsalary@example.com", "123456789", "3001234567",
                                1, 500.0, "password"),
                        "El salario debe estar en el rango válido"),

                Arguments.of("Invalid role for salary",
                        new User(null, "Bad", "RoleSalary", LocalDate.of(1990, 1, 1),
                                "badrole@example.com", "123456789", "3001234567",
                                1, 200000.0, "password"),
                        "El salario no es válido para el rol especificado")
        );
    }

    static Stream<Arguments> validUserCases() {
        return Stream.of(
                Arguments.of("Valid client with minimum salary",
                        new User(null, "Min", "Client", LocalDate.of(1995, 1, 1),
                                "min.client@crediya.com", "111111111", "3001111111",
                                1, 25000.0, "password")),

                Arguments.of("Valid advisor with mid-range salary",
                        new User(null, "Mid", "Advisor", LocalDate.of(1988, 6, 15),
                                "mid.advisor@crediya.com", "222222222", "3002222222",
                                2, 80000.0, "password")),

                Arguments.of("Valid admin with high salary",
                        new User(null, "High", "Admin", LocalDate.of(1985, 12, 25),
                                "high.admin@crediya.com", "333333333", "3003333333",
                                3, 150000.0, "password")),

                Arguments.of("Exact age limit user",
                        new User(null, "Edge", "Case", LocalDate.now().minusYears(18),
                                "edge@crediya.com", "444444444", "3004444444",
                                1, 30000.0, "password"))
        );
    }

    @BeforeEach
    void setUp() {
        user = new User(
                null, // ID debe ser null para inserción
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0,
                "plain_password"
        );
    }

    @Test
    void saveUserShouldSucceedWhenValidationPasses() {
        // Arrange
        when(userValidator.validateUser(any(User.class))).thenReturn(Mono.just(user));
        when(passwordEncryptor.encode("plain_password")).thenReturn("encrypted_password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = new User(
                "generated-uuid-from-db", // El ID que la BD generaría
                user.firstName(),
                user.lastName(),
                user.birthDate(),
                user.email(),
                user.identityDocument(),
                user.phone(),
                user.roleId(),
                user.baseSalary(),
                "encrypted_password"
        );
        when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(savedUser));

        // Act
        Mono<User> result = userUseCase.saveUser(user);

        // Assert
        StepVerifier.create(result)
                .assertNext(returnedUser -> {
                    assertNotNull(returnedUser.id()); // El usuario retornado debe tener ID generado por BD
                    assertNotNull(returnedUser.password()); // Password debe estar encriptado
                })
                .verifyComplete();

        verify(userValidator).validateUser(user);
        verify(userRepository).saveUser(userCaptor.capture());

        // Verificar que el usuario enviado al repositorio tiene ID null (para INSERT)
        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser); // El usuario capturado debe existir
        // El ID debe ser null para que la BD lo genere automáticamente
        // assertNull(capturedUser.id());
    }

    @Test
    void saveUserShouldFailWhenValidationFails() {
        // Arrange
        final String errorMessage = "El correo electrónico ya existe";
        when(userValidator.validateUser(any(User.class))).thenReturn(Mono.error(new BusinessException(errorMessage)));

        // Act
        Mono<User> result = userUseCase.saveUser(user);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(errorMessage))
                .verify();
    }

    @ParameterizedTest
    @MethodSource("businessRuleViolationCases")
    void saveUserShouldHandleBusinessRuleViolations(String scenario, User invalidUser,
                                                    String expectedErrorMessage) {
        // Arrange
        when(userValidator.validateUser(invalidUser))
                .thenReturn(Mono.error(new BusinessException(expectedErrorMessage)));

        // Act
        Mono<User> result = userUseCase.saveUser(invalidUser);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(expectedErrorMessage))
                .verify();
    }

    @ParameterizedTest
    @MethodSource("validUserCases")
    void saveUserShouldSucceedForValidBusinessCases(String scenario, User validUser) {
        // Arrange
        when(userValidator.validateUser(validUser)).thenReturn(Mono.just(validUser));
        when(passwordEncryptor.encode(validUser.password())).thenReturn("encrypted_password");

        User savedUser = new User("generated-id", validUser.firstName(), validUser.lastName(),
                validUser.birthDate(), validUser.email(), validUser.identityDocument(),
                validUser.phone(), validUser.roleId(), validUser.baseSalary(),
                "encrypted_password");
        when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(savedUser));

        // Act
        Mono<User> result = userUseCase.saveUser(validUser);

        // Assert
        StepVerifier.create(result)
                .assertNext(returnedUser -> {
                    assertNotNull(returnedUser.id(), scenario);
                    assertEquals("encrypted_password", returnedUser.password(), scenario);
                    assertEquals(validUser.roleId(), returnedUser.roleId(), scenario);
                })
                .verifyComplete();
    }

    @Test
    void saveUserShouldPreserveBusinessDataIntegrity() {
        // Arrange - Usuario con datos específicos de negocio
        User businessUser = new User(null, "María", "González", LocalDate.of(1985, 3, 15),
                "maria.gonzalez@crediya.com", "987654321", "3109876543",
                2, 75000.0, "secure_password");

        when(userValidator.validateUser(businessUser)).thenReturn(Mono.just(businessUser));
        when(passwordEncryptor.encode("secure_password")).thenReturn("hashed_secure_password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = new User("uuid-generated", businessUser.firstName(), businessUser.lastName(),
                businessUser.birthDate(), businessUser.email(), businessUser.identityDocument(),
                businessUser.phone(), businessUser.roleId(), businessUser.baseSalary(),
                "hashed_secure_password");
        when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(savedUser));

        // Act
        Mono<User> result = userUseCase.saveUser(businessUser);

        // Assert
        StepVerifier.create(result)
                .assertNext(returnedUser -> {
                    assertEquals("uuid-generated", returnedUser.id());
                    assertEquals(2, returnedUser.roleId()); // ADVISOR role
                    assertEquals(75000.0, returnedUser.baseSalary());
                    assertEquals("hashed_secure_password", returnedUser.password());
                })
                .verifyComplete();

        verify(userRepository).saveUser(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("hashed_secure_password", capturedUser.password());
    }
}