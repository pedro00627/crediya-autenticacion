package co.com.pragma.config;

import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.user.UserUseCase;
import co.com.pragma.usecase.validation.UserValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public UserValidator userValidator(UserRepository userRepository, RoleRepository roleRepository) {
        return new UserValidator(userRepository, roleRepository);
    }

    @Bean
    public UserUseCase userUseCase(UserRepository userRepository, UserValidator userValidator, PasswordEncryptor passwordEncryptor) {
        return new UserUseCase(userRepository, userValidator, passwordEncryptor);
    }
}
