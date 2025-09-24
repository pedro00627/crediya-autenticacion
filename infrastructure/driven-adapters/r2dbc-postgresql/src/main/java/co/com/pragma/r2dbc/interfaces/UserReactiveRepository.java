package co.com.pragma.r2dbc.interfaces;

import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entity.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, String>, ReactiveQueryByExampleExecutor<UserEntity> {
    Mono<Boolean> existsByEmail(String email);

    Mono<User> findByEmail(String email);

    Flux<User> findByEmailOrIdentityDocument(String email, String identityDocument);
}
