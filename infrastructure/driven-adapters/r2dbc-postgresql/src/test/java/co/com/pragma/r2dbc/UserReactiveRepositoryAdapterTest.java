package co.com.pragma.r2dbc;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entity.UserEntity;
import co.com.pragma.r2dbc.interfaces.UserReactiveRepository;
import co.com.pragma.r2dbc.mapper.UserDataMapper;
import co.com.pragma.r2dbc.repository.UserReactiveRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {

    // Eliminar @InjectMocks de aquí
    UserReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    UserReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Mock
    UserDataMapper userDataMapper;

    @Mock
    TransactionalOperator transactionalOperator;

    @Mock
    LoggerPort logger;

    private User user;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        // Inicializar repositoryAdapter manualmente, pasando todos los mocks
        this.repositoryAdapter = new UserReactiveRepositoryAdapter(this.repository, this.mapper, this.logger, this.transactionalOperator, this.userDataMapper);

        // Se crean objetos de ejemplo para usar en todos los tests
        this.user = new User(
                "1",
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0,
                ""
        );

        this.userEntity = new UserEntity(
                "1",
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0,
                ""
        );
    }

    @Test
    void mustFindValueById() {
        // Arrange: Configurar los mocks para que devuelvan los objetos correctos
        when(this.repository.findById("1")).thenReturn(Mono.just(this.userEntity));
        when(this.userDataMapper.toDomain(this.userEntity)).thenReturn(this.user);

        // Act: Llamar al método que se está probando
        final Mono<User> result = this.repositoryAdapter.findById("1");

        // Assert: Verificar que el resultado es el esperado
        StepVerifier.create(result)
                .expectNext(this.user)
                .verifyComplete();
    }

    @Test
    void mustFindAllValues() {
        // Arrange
        when(this.repository.findAll()).thenReturn(Flux.just(this.userEntity));
        when(this.userDataMapper.toDomain(this.userEntity)).thenReturn(this.user);

        // Act
        final Flux<User> result = this.repositoryAdapter.findAll();

        // Assert
        StepVerifier.create(result)
                .expectNext(this.user)
                .verifyComplete();
    }

    @Test
    void mustFindByExample() {
        // Arrange
        when(this.mapper.map(this.user, UserEntity.class)).thenReturn(this.userEntity);
        when(this.repository.findAll(any(Example.class))).thenReturn(Flux.just(this.userEntity));
        when(this.userDataMapper.toDomain(this.userEntity)).thenReturn(this.user);

        // Act
        final Flux<User> result = this.repositoryAdapter.findByExample(this.user);

        // Assert
        StepVerifier.create(result)
                .expectNext(this.user)
                .verifyComplete();
    }

    @Test
    void mustSaveValue() {
        // Arrange
        // Mock para el operador transaccional, simplemente devuelve el Mono que recibe
        when(this.transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.userDataMapper.toEntity(this.user)).thenReturn(this.userEntity);
        when(this.repository.save(any(UserEntity.class))).thenReturn(Mono.just(this.userEntity));
        when(this.userDataMapper.toDomain(this.userEntity)).thenReturn(this.user);

        // Act
        final Mono<User> result = this.repositoryAdapter.saveUser(this.user);

        // Assert
        StepVerifier.create(result)
                .expectNext(this.user)
                .verifyComplete();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Arrange
        final String email = "john.doe@example.com";
        when(this.repository.existsByEmail(email)).thenReturn(Mono.just(true));

        // Act
        final Mono<Boolean> result = this.repositoryAdapter.existByEmail(email);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }
}
