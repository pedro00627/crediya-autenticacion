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
    protected ReactiveAdapterOperations(final LoggerPort logger, final R repository, final ObjectMapper mapper, final Function<D, E> toEntityFn) {
        this.logger = logger;
        this.repository = repository;
        this.mapper = mapper;
        final ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        dataClass = (Class<D>) genericSuperclass.getActualTypeArguments()[1];
        this.toEntityFn = toEntityFn;
    }

    protected D toData(final E entity) {
        return this.mapper.map(entity, this.dataClass);
    }

    protected E toEntity(final D data) {
        return null != data ? this.toEntityFn.apply(data) : null;
    }

    public Mono<E> saveUser(final E entity) {
        this.logger.debug("Mapeando entidad a objeto de datos para guardar: {}", entity);
        return this.saveData(this.toData(entity))
                .map(this::toEntity);
    }

    protected Flux<E> saveAllEntities(final Flux<E> entities) {
        return this.saveData(entities.map(this::toData))
                .map(this::toEntity);
    }

    protected Mono<D> saveData(final D data) {
        this.logger.debug("Llamando a repository.save con datos: {}", data);
        return this.repository.save(data)
                .doOnError(err -> this.logger.error("Error al guardar datos en el repositorio", err));
    }

    protected Flux<D> saveData(final Flux<D> data) {
        return this.repository.saveAll(data);
    }

    public Mono<E> findById(final I id) {
        this.logger.debug("Buscando por ID: {}", id);
        return this.repository.findById(id)
                .doOnNext(data -> this.logger.debug("Encontrado para ID {}: {}", id, data))
                .map(this::toEntity);
    }

    public Flux<E> findByExample(final E entity) {
        this.logger.debug("Buscando por ejemplo: {}", entity);
        return this.repository.findAll(Example.of(this.toData(entity)))
                .map(this::toEntity);
    }

    public Flux<E> findAll() {
        this.logger.debug("Buscando todas las entidades");
        return this.repository.findAll()
                .map(this::toEntity);
    }
}
