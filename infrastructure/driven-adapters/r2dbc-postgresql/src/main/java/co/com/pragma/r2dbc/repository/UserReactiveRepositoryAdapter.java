package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.repository.UserRepository;
import co.com.pragma.r2dbc.entity.UserEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.interfaces.UserReactiveRepository;
import co.com.pragma.r2dbc.mapper.UserDataMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        UserReactiveRepository
        > implements UserRepository {

    private final LoggerPort logger;
    private final TransactionalOperator transactionalOperator;
    private final UserDataMapper userDataMapper;

    public UserReactiveRepositoryAdapter(final UserReactiveRepository repository, final ObjectMapper mapper, final LoggerPort logger, final TransactionalOperator transactional, final UserDataMapper userDataMapper) {
        super(logger, repository, mapper, userDataMapper::toDomain);
        this.logger = logger;
        transactionalOperator = transactional;
        this.userDataMapper = userDataMapper;
    }

    @Override
    public Mono<User> saveUser(final User user) {
        final UserEntity userEntity = this.userDataMapper.toEntity(user);
        return this.repository.save(userEntity)
                .doOnSubscribe(subscription -> this.logger.info("Guardando usuario en la base de datos con email: {}", this.logger.maskEmail(user.email())))
                .map(this.userDataMapper::toDomain) // Map the saved entity back to the domain model
                .doOnSuccess(savedUser -> this.logger.info("Usuario guardado exitosamente en BD con ID: {}", savedUser.id()))
                // Añadimos un log específico para el caso de error durante el guardado
                .doOnError(error -> this.logger.error("Error al guardar el usuario", error))
                .as(this.transactionalOperator::transactional);
    }

    @Override
    public Mono<Boolean> existByEmail(final String email) {
        this.logger.debug("Verificando existencia de email en BD: {}", this.logger.maskEmail(email));
        return this.repository.existsByEmail(email);
    }

    @Override
    public Mono<User> getUserByEmail(final String email) {
        this.logger.debug("Buscando usuario por email en BD: {}", this.logger.maskEmail(email));
        return this.repository.findByEmail(email);
    }

    public Flux<User> getUserByEmailOrIdentityDocument(final String email, final String identityDocument) {
        this.logger.debug("Buscando usuario por email o documento de identidad en BD: {} - {}", this.logger.maskEmail(email), identityDocument);
        return this.repository.findByEmailOrIdentityDocument(email, identityDocument);
    }

}
