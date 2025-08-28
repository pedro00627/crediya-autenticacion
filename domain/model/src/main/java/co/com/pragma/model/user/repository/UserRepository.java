package co.com.pragma.model.user.repository;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> saveUser(User user);

    Mono<Boolean> existByEmail(String email);
}
