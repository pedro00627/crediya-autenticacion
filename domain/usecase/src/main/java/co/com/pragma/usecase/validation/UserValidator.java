package co.com.pragma.usecase.validation;

import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import reactor.core.publisher.Mono;

import static co.com.pragma.usecase.validation.ValidationConstants.EMAIL_ALREADY_EXISTS_MESSAGE;
import static co.com.pragma.usecase.validation.ValidationConstants.MAX_BASE_SALARY;
import static co.com.pragma.usecase.validation.ValidationConstants.MIN_BASE_SALARY;
import static co.com.pragma.usecase.validation.ValidationConstants.ROLE_NOT_FOUND_MESSAGE;
import static co.com.pragma.usecase.validation.ValidationConstants.SALARY_OUT_OF_RANGE_MESSAGE;

public class UserValidator {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserValidator(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Mono<User> validateUser(User user) {
        return Mono.when(
                        validateSalaryRange(user),
                        validateRoleExistence(user),
                        validateEmailUniqueness(user)
                )
                .then(Mono.just(user));
    }

    private Mono<Void> validateSalaryRange(User user) {
        if (MIN_BASE_SALARY <= user.baseSalary() && MAX_BASE_SALARY >= user.baseSalary()) {
            return Mono.empty();
        }
        return Mono.error(new BusinessException(SALARY_OUT_OF_RANGE_MESSAGE));
    }

    private Mono<Void> validateRoleExistence(User user) {
        if (null == user.roleId()) {
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