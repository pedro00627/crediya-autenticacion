package co.com.pragma.r2dbc.helper;

import co.com.pragma.model.log.gateways.LoggerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveAdapterOperationsTest {

    @Mock
    private DummyRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private LoggerPort logger;

    private ReactiveAdapterOperations<DummyEntity, DummyData, String, DummyRepository> operations;

    @BeforeEach
    void setUp() {
        this.operations = new TestReactiveAdapterOperations(
                this.logger, this.repository, this.mapper, DummyEntity::toEntity);
    }

    @Test
    void save() {
        final DummyEntity entity = new DummyEntity("1", "test");
        final DummyData data = new DummyData("1", "test");

        when(this.mapper.map(entity, DummyData.class)).thenReturn(data);
        when(this.repository.save(data)).thenReturn(Mono.just(data));

        StepVerifier.create(this.operations.saveUser(entity))
                .expectNext(entity)
                .verifyComplete();
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void saveAllEntities() {
        final DummyEntity entity1 = new DummyEntity("1", "test1");
        final DummyEntity entity2 = new DummyEntity("2", "test2");
        final DummyData data1 = new DummyData("1", "test1");
        final DummyData data2 = new DummyData("2", "test2");

        // Cambiar este stubbing para manejar múltiples llamadas secuenciales
        when(this.mapper.map(any(DummyEntity.class), eq(DummyData.class)))
                .thenAnswer(invocation -> {
                    final DummyEntity entity = invocation.getArgument(0);
                    return new DummyData(entity.getId(), entity.getName());
                });

        when(this.repository.saveAll(any(Flux.class))).thenReturn(Flux.just(data1, data2));

        StepVerifier.create(this.operations.saveAllEntities(Flux.just(entity1, entity2)))
                .expectNext(entity1, entity2)
                .verifyComplete();
    }

    @Test
    void findById() {
        final DummyData data = new DummyData("1", "test");
        final DummyEntity entity = new DummyEntity("1", "test");

        when(this.repository.findById("1")).thenReturn(Mono.just(data));

        StepVerifier.create(this.operations.findById("1"))
                .expectNext(entity)
                .verifyComplete();
    }

    @Test
    void findByExample() {
        final DummyEntity entity = new DummyEntity("1", "test");
        final DummyData data = new DummyData("1", "test");

        when(this.mapper.map(entity, DummyData.class)).thenReturn(data);
        when(this.repository.findAll(any(Example.class))).thenReturn(Flux.just(data));

        StepVerifier.create(this.operations.findByExample(entity))
                .expectNext(entity)
                .verifyComplete();
    }

    @Test
    void findAll() {
        final DummyData data1 = new DummyData("1", "test1");
        final DummyData data2 = new DummyData("2", "test2");
        final DummyEntity entity1 = new DummyEntity("1", "test1");
        final DummyEntity entity2 = new DummyEntity("2", "test2");

        when(this.repository.findAll()).thenReturn(Flux.just(data1, data2));

        StepVerifier.create(this.operations.findAll())
                .expectNext(entity1, entity2)
                .verifyComplete();
    }

    interface DummyRepository extends ReactiveCrudRepository<DummyData, String>, ReactiveQueryByExampleExecutor<DummyData> {
    }

    static class TestReactiveAdapterOperations extends ReactiveAdapterOperations<DummyEntity, DummyData, String, DummyRepository> {
        public TestReactiveAdapterOperations(final LoggerPort logger, final DummyRepository repository, final ObjectMapper mapper, final Function<DummyData, DummyEntity> toEntityFn) {
            super(logger, repository, mapper, toEntityFn);
        }
    }

    static class DummyEntity {
        private final String id;
        private final String name;

        public DummyEntity(final String id, final String name) {
            this.id = id;
            this.name = name;
        }

        public static DummyEntity toEntity(final DummyData data) {
            return new DummyEntity(data.getId(), data.getName());
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (null == o || this.getClass() != o.getClass()) return false;
            final DummyEntity that = (DummyEntity) o;
            return this.id.equals(that.id) && this.name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id, this.name);
        }
    }

    static class DummyData {
        private final String id;
        private final String name;

        public DummyData(final String id, final String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (null == o || this.getClass() != o.getClass()) return false;
            final DummyData that = (DummyData) o;
            return this.id.equals(that.id) && this.name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id, this.name);
        }
    }
}
