package co.com.pragma.usecase.user;

import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

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

    @BeforeEach
    void setUp() {
        this.user = new User(
                null,
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0,
                null
        );
    }

    @Test
    void saveUserShouldSucceedWhenValidationPasses() {
        // Arrange
        final User savedUser = new User(
                "id-123",
                this.user.firstName(),
                this.user.lastName(),
                this.user.birthDate(),
                this.user.email(),
                this.user.identityDocument(),
                this.user.phone(),
                this.user.roleId(),
                this.user.baseSalary(),
                this.user.password()
        );
        when(this.userValidator.validateUser(any(User.class))).thenReturn(Mono.just(this.user));
        when(this.userRepository.saveUser(any(User.class))).thenReturn(Mono.just(savedUser));

        // Act
        final Mono<User> result = this.userUseCase.saveUser(this.user);

        // Assert
        StepVerifier.create(result)
                .expectNext(savedUser)
                .verifyComplete();

        verify(this.userValidator).validateUser(this.user);
        verify(this.userRepository).saveUser(this.user);
    }

    @Test
    void saveUserShouldFailWhenValidationFails() {
        // Arrange
        final String errorMessage = "El correo electrónico ya existe";
        when(this.userValidator.validateUser(any(User.class))).thenReturn(Mono.error(new BusinessException(errorMessage)));

        // Act
        final Mono<User> result = this.userUseCase.saveUser(this.user);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(errorMessage))
                .verify();
    }
}