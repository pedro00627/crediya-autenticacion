package co.com.pragma.config;

import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.user.UserUseCase;
import co.com.pragma.usecase.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration()
class UseCaseConfigTest {
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private UserValidator userValidator;
    @MockitoBean
    private PasswordEncryptor passwordEncryptor;

    @MockitoBean
    private UserUseCase userUseCase;

    @Test
    void testUserUseCaseBeanIsCreated() {
        assertNotNull(userUseCase, "UserUseCase bean should not be null");
    }

}
