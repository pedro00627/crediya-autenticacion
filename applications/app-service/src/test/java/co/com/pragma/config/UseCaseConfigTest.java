package co.com.pragma.config;

import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.user.UserUseCase;
import co.com.pragma.usecase.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class UseCaseConfigTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncryptor passwordEncryptor;

    private UseCaseConfig useCaseConfig;

    @BeforeEach
    void setUp() {
        useCaseConfig = new UseCaseConfig();
    }

    @Test
    void shouldCreateUserValidator() {
        UserValidator result = useCaseConfig.userValidator(userRepository, roleRepository);

        assertNotNull(result);
        assertInstanceOf(UserValidator.class, result);
    }

    @Test
    void shouldCreateUserUseCase() {
        UserValidator userValidator = new UserValidator(userRepository, roleRepository);

        UserUseCase result = useCaseConfig.userUseCase(userRepository, userValidator, passwordEncryptor);

        assertNotNull(result);
        assertInstanceOf(UserUseCase.class, result);
    }
}
