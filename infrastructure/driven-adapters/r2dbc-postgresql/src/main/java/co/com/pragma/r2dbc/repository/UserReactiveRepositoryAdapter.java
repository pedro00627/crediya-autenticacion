package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.r2dbc.entity.UserEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.interfaces.UserReactiveRepository;
import co.com.pragma.r2dbc.mapper.UserDataMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        UserReactiveRepository
        > implements UserRepository {

    private static final Logger log = LogManager.getLogger(UserReactiveRepositoryAdapter.class);
    private final TransactionalOperator transactionalOperator;
    private final UserDataMapper userDataMapper;

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactional, UserDataMapper userDataMapper) {
        super(repository, mapper, userDataMapper::toDomain);
        this.transactionalOperator = transactional;
        this.userDataMapper = userDataMapper;
    }

    @Override
    public Mono<User> saveUser(User user) {
        UserEntity userEntity = userDataMapper.toEntity(user);
        return repository.save(userEntity)
                .doOnSubscribe(subscription ->log.info("Guardando usuario en la base de datos con email: {}", user.email()))
                .map(userDataMapper::toDomain) // Map the saved entity back to the domain model
                .doOnSuccess(savedUser -> log.info("Usuario guardado exitosamente en BD con ID: {}", savedUser.id()))
                // Añadimos un log específico para el caso de error durante el guardado
                .doOnError(error -> log.error("Error al guardar el usuario con email: {}", user.email(), error))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Boolean> existByEmail(String email) {
        log.debug("Verificando existencia de email en BD: {}", email);
        return repository.existsByEmail(email);
    }

}
