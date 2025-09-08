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
import org.mockito.InjectMocks;
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
        repositoryAdapter = new UserReactiveRepositoryAdapter(repository, mapper, logger, transactionalOperator, userDataMapper);

        // Se crean objetos de ejemplo para usar en todos los tests
        user = new User(
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

        userEntity = new UserEntity(
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
        when(repository.findById("1")).thenReturn(Mono.just(userEntity));
        when(userDataMapper.toDomain(userEntity)).thenReturn(user);

        // Act: Llamar al método que se está probando
        Mono<User> result = repositoryAdapter.findById("1");

        // Assert: Verificar que el resultado es el esperado
        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void mustFindAllValues() {
        // Arrange
        when(repository.findAll()).thenReturn(Flux.just(userEntity));
        when(userDataMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Flux<User> result = repositoryAdapter.findAll();

        // Assert
        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void mustFindByExample() {
        // Arrange
        when(mapper.map(user, UserEntity.class)).thenReturn(userEntity);
        when(repository.findAll(any(Example.class))).thenReturn(Flux.just(userEntity));
        when(userDataMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Flux<User> result = repositoryAdapter.findByExample(user);

        // Assert
        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void mustSaveValue() {
        // Arrange
        // Mock para el operador transaccional, simplemente devuelve el Mono que recibe
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDataMapper.toEntity(user)).thenReturn(userEntity);
        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(userEntity));
        when(userDataMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Mono<User> result = repositoryAdapter.saveUser(user);

        // Assert
        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Arrange
        String email = "john.doe@example.com";
        when(repository.existsByEmail(email)).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = repositoryAdapter.existByEmail(email);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }
}
