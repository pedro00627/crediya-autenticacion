package co.com.pragma.usecase.user;

import co.com.pragma.model.security.PasswordEncryptor;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.usecase.validation.UserValidator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserUseCase {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncryptor passwordEncryptor;

    public UserUseCase(final UserRepository userRepository, final UserValidator userValidator, final PasswordEncryptor passwordEncryptor) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.passwordEncryptor = passwordEncryptor;
    }

    public Mono<User> saveUser(final User user) {
        return this.userValidator.validateUser(user)
                .map(userToSave -> {
                    final String encodedPassword = this.passwordEncryptor.encode(userToSave.password());
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
                .flatMap(this.userRepository::saveUser);
    }

    public Mono<User> getUserByEmail(final String email) {
        return this.userRepository.getUserByEmail(email);
    }

    public Flux<User> getUserByEmailOrIdentityDocument(final String email, final String identityDocument) {
        return this.userRepository.getUserByEmailOrIdentityDocument(email, identityDocument);
    }
}
