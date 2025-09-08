package co.com.pragma.config;

import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.user.UserUseCase;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.usecase.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UseCaseConfigTest.TestConfig.class)
class UseCaseConfigTest {

    @Autowired
    private UserUseCase userUseCase;

    @Test
    void testUserUseCaseBeanIsCreated() {
        assertNotNull(userUseCase, "UserUseCase bean should not be null");
    }

    @Configuration
    static class TestConfig {
        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public RoleRepository roleRepository() {
            return Mockito.mock(RoleRepository.class);
        }

        @Bean
        public PasswordEncryptor passwordEncryptor() {
            return Mockito.mock(PasswordEncryptor.class);
        }

        @Bean
        public UserValidator userValidator(UserRepository userRepository, RoleRepository roleRepository) {
            return new UserValidator(userRepository, roleRepository);
        }

        @Bean
        public UserUseCase userUseCase(UserRepository userRepository, UserValidator userValidator, PasswordEncryptor passwordEncryptor) {
            return new UserUseCase(userRepository, userValidator, passwordEncryptor);
        }
    }
}
