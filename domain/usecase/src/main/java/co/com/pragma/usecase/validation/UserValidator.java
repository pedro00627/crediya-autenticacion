package co.com.pragma.usecase.validation;

import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import reactor.core.publisher.Mono;

public class UserValidator {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserValidator(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Mono<User> validateUser(User user) {
        // 1. Validación síncrona: Rango de salario
        if (user.baseSalary() < 0 || user.baseSalary() > 15000000) {
            return Mono.error(new BusinessException("El salario base debe estar entre 0 y 15,000,000."));
        }

        // 2. Validaciones asíncronas
        Mono<Void> roleValidation = validateRoleExistence(user);
        Mono<Void> emailValidation = validateEmailUniqueness(user);

        // 3. Ejecutar validaciones asíncronas en paralelo y proceder si ambas son exitosas
        return Mono.when(roleValidation, emailValidation)
                .then(Mono.just(user));
    }

    private Mono<Void> validateRoleExistence(User user) {
        return user.roleId() == null ? Mono.empty() :
                roleRepository.existsById(user.roleId())
                        .flatMap(exists -> !exists ? Mono.error(new BusinessException("El rol con ID '" + user.roleId() + "' no existe.")) : Mono.empty());
    }

    private Mono<Void> validateEmailUniqueness(User user) {
        return userRepository.existByEmail(user.email())
                .flatMap(exists -> exists ? Mono.error(new BusinessException("El correo electrónico '" + user.email() + "' ya se encuentra registrado.")) : Mono.empty());
    }
}