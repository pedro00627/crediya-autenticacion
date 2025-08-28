package co.com.pragma.config;

import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.user.UserUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UseCaseConfigTest.TestConfig.class)
public class UseCaseConfigTest {

    @Autowired
    private UserUseCase userUseCase;

    @Test
    void testUserUseCaseBeanIsCreated() {
        assertNotNull(userUseCase, "UserUseCase bean should not be null");
    }

    @Configuration
    @Import(UseCaseConfig.class) // Import the real configuration
    static class TestConfig {
        // Provide a mock dependency for the real UseCaseConfig to use
        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public RoleRepository roleRepository() {
            return Mockito.mock(RoleRepository.class);
        }
    }
}
