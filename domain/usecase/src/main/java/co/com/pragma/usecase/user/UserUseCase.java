package co.com.pragma.usecase.user;

import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.validation.UserValidator;
import reactor.core.publisher.Mono;

public class UserUseCase {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncryptor passwordEncryptor;

    public UserUseCase(UserRepository userRepository, UserValidator userValidator, PasswordEncryptor passwordEncryptor) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.passwordEncryptor = passwordEncryptor;
    }

    public Mono<User> saveUser(User user) {
        return userValidator.validateUser(user)
                .map(userToSave -> {
                    String encodedPassword = passwordEncryptor.encode(userToSave.password());
                    return new User(
                            userToSave.id(),
                            userToSave.firstName(),
                            userToSave.lastName(),
                            userToSave.birthDate(),
                            userToSave.email(),
                            userToSave.identityDocument(),
                            userToSave.phone(),
                            userToSave.roleId(),
                            userToSave.baseSalary(),
                            encodedPassword
                    );
                })
                .flatMap(userRepository::saveUser);
    }

    public Mono<User> getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }
}
