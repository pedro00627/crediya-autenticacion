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

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper, LoggerPort logger, TransactionalOperator transactional, UserDataMapper userDataMapper) {
        super(logger, repository, mapper, userDataMapper::toDomain);
        this.logger = logger;
        this.transactionalOperator = transactional;
        this.userDataMapper = userDataMapper;
    }

    @Override
    public Mono<User> saveUser(User user) {
        UserEntity userEntity = userDataMapper.toEntity(user);
        return repository.save(userEntity)
                .doOnSubscribe(subscription -> logger.info("Guardando usuario en la base de datos con email: {}", logger.maskEmail(user.email())))
                .map(userDataMapper::toDomain) // Map the saved entity back to the domain model
                .doOnSuccess(savedUser -> logger.info("Usuario guardado exitosamente en BD con ID: {}", savedUser.id()))
                // Añadimos un log específico para el caso de error durante el guardado
                .doOnError(error -> logger.error("Error al guardar el usuario", error))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Boolean> existByEmail(String email) {
        logger.debug("Verificando existencia de email en BD: {}", logger.maskEmail(email));
        return repository.existsByEmail(email);
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        logger.debug("Buscando usuario por email en BD: {}", logger.maskEmail(email));
        return repository.findByEmail(email);
    }

    public Flux<User> getUserByEmailOrIdentityDocument(String email, String identityDocument) {
        logger.debug("Buscando usuario por email o documento de identidad en BD: {} - {}", logger.maskEmail(email), identityDocument);
        return repository.findByEmailOrIdentityDocument(email, identityDocument);
    }

}
