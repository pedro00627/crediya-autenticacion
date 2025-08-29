package co.com.pragma.usecase.validation;

import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import reactor.core.publisher.Mono;

import static co.com.pragma.usecase.validation.ValidationConstants.*;

public class UserValidator {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserValidator(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Mono<User> validateUser(User user) {
        // Ejecutar todas las validaciones en paralelo.
        // Si alguna falla, el Mono resultante fallará.
        return Mono.when(
                        validateSalaryRange(user),
                        validateRoleExistence(user),
                        validateEmailUniqueness(user)
                )
                // Si todas las validaciones son exitosas, se emite el usuario original.
                .then(Mono.just(user));
    }

    private Mono<Void> validateSalaryRange(User user) {
        if (user.baseSalary() >= MIN_BASE_SALARY && user.baseSalary() <= MAX_BASE_SALARY) {
            return Mono.empty(); // Salario válido, el Mono se completa.
        }
        return Mono.error(new BusinessException(SALARY_OUT_OF_RANGE_MESSAGE));
    }

    private Mono<Void> validateRoleExistence(User user) {
        // Si no se provee un roleId, no se valida.
        if (user.roleId() == null) {
            return Mono.empty();
        }
        return roleRepository.existsById(user.roleId())
                .filter(exists -> exists) // Deja pasar el flujo solo si 'exists' es true.
                .switchIfEmpty(Mono.error(new BusinessException(String.format(ROLE_NOT_FOUND_MESSAGE, user.roleId()))))
                .then(); // Convierte el Mono<Boolean> a Mono<Void>, ya que solo nos importa si hubo un error o no.
    }

    private Mono<Void> validateEmailUniqueness(User user) {
        return userRepository.existByEmail(user.email())
                .filter(exists -> !exists) // Deja pasar el flujo solo si 'exists' es false.
                .switchIfEmpty(Mono.error(new BusinessException(String.format(EMAIL_ALREADY_EXISTS_MESSAGE, user.email()))))
                .then(); // Convierte el Mono<Boolean> a Mono<Void>.
    }
}