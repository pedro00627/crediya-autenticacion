package co.com.pragma.r2dbc.helper;

import co.com.pragma.model.log.gateways.LoggerPort;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

public abstract class ReactiveAdapterOperations<E, D, I, R extends ReactiveCrudRepository<D, I> & ReactiveQueryByExampleExecutor<D>> {
    private final LoggerPort logger;
    private final Class<D> dataClass;
    private final Function<D, E> toEntityFn;
    protected R repository;
    protected ObjectMapper mapper;

    @SuppressWarnings("unchecked")
    protected ReactiveAdapterOperations(LoggerPort logger, R repository, ObjectMapper mapper, Function<D, E> toEntityFn) {
        this.logger = logger;
        this.repository = repository;
        this.mapper = mapper;
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.dataClass = (Class<D>) genericSuperclass.getActualTypeArguments()[1];
        this.toEntityFn = toEntityFn;
    }

    protected D toData(E entity) {
        return mapper.map(entity, dataClass);
    }

    protected E toEntity(D data) {
        return data != null ? toEntityFn.apply(data) : null;
    }

    public Mono<E> saveUser(E entity) {
        logger.debug("Mapeando entidad a objeto de datos para guardar: {}", entity);
        return saveData(toData(entity))
                .map(this::toEntity);
    }

    protected Flux<E> saveAllEntities(Flux<E> entities) {
        return saveData(entities.map(this::toData))
                .map(this::toEntity);
    }

    protected Mono<D> saveData(D data) {
        logger.debug("Llamando a repository.save con datos: {}", data);
        return repository.save(data)
                .doOnError(err -> logger.error("Error al guardar datos en el repositorio", err));
    }

    protected Flux<D> saveData(Flux<D> data) {
        return repository.saveAll(data);
    }

    public Mono<E> findById(I id) {
        logger.debug("Buscando por ID: {}", id);
        return repository.findById(id)
                .doOnNext(data -> logger.debug("Encontrado para ID {}: {}", id, data))
                .map(this::toEntity);
    }

    public Flux<E> findByExample(E entity) {
        logger.debug("Buscando por ejemplo: {}", entity);
        return repository.findAll(Example.of(toData(entity)))
                .map(this::toEntity);
    }

    public Flux<E> findAll() {
        logger.debug("Buscando todas las entidades");
        return repository.findAll()
                .map(this::toEntity);
    }
}
