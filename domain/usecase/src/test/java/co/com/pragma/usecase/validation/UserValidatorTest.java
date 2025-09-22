package co.com.pragma.usecase.validation;

import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static co.com.pragma.usecase.validation.ValidationConstants.EMAIL_ALREADY_EXISTS_MESSAGE;
import static co.com.pragma.usecase.validation.ValidationConstants.MAX_BASE_SALARY;
import static co.com.pragma.usecase.validation.ValidationConstants.MIN_BASE_SALARY;
import static co.com.pragma.usecase.validation.ValidationConstants.ROLE_NOT_FOUND_MESSAGE;
import static co.com.pragma.usecase.validation.ValidationConstants.SALARY_OUT_OF_RANGE_MESSAGE;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserValidator userValidator;

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
                ""
        );
    }

    @Test
    void validateUserShouldSucceedWhenAllValidationsPass() {
        // Arrange
        when(this.userRepository.existByEmail(this.user.email())).thenReturn(Mono.just(false));
        when(this.roleRepository.existsById(this.user.roleId())).thenReturn(Mono.just(true));

        // Act
        final Mono<User> result = this.userValidator.validateUser(this.user);

        // Assert
        StepVerifier.create(result)
                .expectNext(this.user)
                .verifyComplete();
    }

    @Test
    void validateUserShouldFailWhenSalaryIsOutOfRange() {
        // Arrange
        final User userWithInvalidSalary = new User(
                null, "Jane", "Doe", LocalDate.now(), "jane.doe@example.com",
                "987654321", "3109876543", 2, MAX_BASE_SALARY + 1, ""
        );

        // Stub other parallel validations to ensure they don't fail with NPE
        when(this.userRepository.existByEmail(userWithInvalidSalary.email())).thenReturn(Mono.just(false));
        when(this.roleRepository.existsById(userWithInvalidSalary.roleId())).thenReturn(Mono.just(true));

        // Act
        final Mono<User> result = this.userValidator.validateUser(userWithInvalidSalary);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(SALARY_OUT_OF_RANGE_MESSAGE))
                .verify();
    }

    @Test
    void validateUserShouldFailWhenRoleDoesNotExist() {
        // Arrange
        when(this.userRepository.existByEmail(this.user.email())).thenReturn(Mono.just(false));
        when(this.roleRepository.existsById(this.user.roleId())).thenReturn(Mono.just(false)); // Role does not exist

        // Act
        final Mono<User> result = this.userValidator.validateUser(this.user);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(String.format(ROLE_NOT_FOUND_MESSAGE, this.user.roleId())))
                .verify();
    }

    @Test
    void validateUserShouldFailWhenEmailAlreadyExists() {
        // Arrange
        when(this.userRepository.existByEmail(this.user.email())).thenReturn(Mono.just(true)); // Email exists
        when(this.roleRepository.existsById(this.user.roleId())).thenReturn(Mono.just(true));

        // Act
        final Mono<User> result = this.userValidator.validateUser(this.user);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(String.format(EMAIL_ALREADY_EXISTS_MESSAGE, this.user.email())))
                .verify();
    }

    @Test
    void validateUserShouldSucceedWhenRoleIdIsNull() {
        // Arrange
        final User userWithNullRole = new User(
                null, "Sam", "Smith", LocalDate.now(), "sam.smith@example.com",
                "555555555", "3205555555", null, 100000.0, ""
        );
        when(this.userRepository.existByEmail(userWithNullRole.email())).thenReturn(Mono.just(false));

        // Act
        final Mono<User> result = this.userValidator.validateUser(userWithNullRole);

        // Assert
        StepVerifier.create(result)
                .expectNext(userWithNullRole)
                .verifyComplete();

        // Verify that the role repository was never called
        verify(this.roleRepository, never()).existsById(any());
    }

    @Test
    void validateUserShouldFailWhenSalaryIsBelowRange() {
        // Arrange
        final User userWithNegativeSalary = new User(
                null, "Negative", "Salary", LocalDate.now(), "negative.salary@example.com",
                "111222333", "3001112222", 3, -100.0, ""
        );

        // Stub other parallel validations to ensure they don't fail with NPE
        when(this.userRepository.existByEmail(userWithNegativeSalary.email())).thenReturn(Mono.just(false));
        when(this.roleRepository.existsById(userWithNegativeSalary.roleId())).thenReturn(Mono.just(true));

        // Act
        final Mono<User> result = this.userValidator.validateUser(userWithNegativeSalary);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(SALARY_OUT_OF_RANGE_MESSAGE))
                .verify();
    }

    @ParameterizedTest
    @ValueSource(doubles = {MIN_BASE_SALARY, MAX_BASE_SALARY})
    void validateUserShouldSucceedWhenSalaryIsOnBoundaries(final double boundarySalary) {
        // Arrange
        final User userWithBoundarySalary = new User(null, "Boundary", "Test", LocalDate.now(), "boundary@example.com", "444555666", "3154445555", 1, boundarySalary, "");
        when(this.userRepository.existByEmail(userWithBoundarySalary.email())).thenReturn(Mono.just(false));
        when(this.roleRepository.existsById(userWithBoundarySalary.roleId())).thenReturn(Mono.just(true));

        // Act
        final Mono<User> result = this.userValidator.validateUser(userWithBoundarySalary);

        // Assert
        StepVerifier.create(result)
                .expectNext(userWithBoundarySalary)
                .verifyComplete();
    }
}