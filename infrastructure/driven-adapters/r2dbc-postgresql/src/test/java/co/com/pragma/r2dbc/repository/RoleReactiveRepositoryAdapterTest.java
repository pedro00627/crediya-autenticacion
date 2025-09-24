package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.r2dbc.interfaces.RoleReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleReactiveRepositoryAdapterTest {

    @Mock
    private RoleReactiveRepository roleReactiveRepository;
    @Mock
    private TransactionalOperator transactionalOperator;

    private RoleReactiveRepositoryAdapter roleReactiveRepositoryAdapter;

    @BeforeEach
    void setUp() {
        roleReactiveRepositoryAdapter = new RoleReactiveRepositoryAdapter(roleReactiveRepository, transactionalOperator);
    }

    @Test
    void shouldImplementRoleRepository() {
        assertInstanceOf(RoleRepository.class, roleReactiveRepositoryAdapter);
    }

    @Test
    void shouldReturnTrueWhenRoleExists() {
        Integer roleId = 1;
        Mono<Boolean> existsMono = Mono.just(true);

        when(roleReactiveRepository.existsById(roleId)).thenReturn(existsMono);
        when(transactionalOperator.transactional(existsMono)).thenReturn(existsMono);

        StepVerifier.create(roleReactiveRepositoryAdapter.existsById(roleId))
                .expectNext(true)
                .verifyComplete();

        verify(roleReactiveRepository).existsById(roleId);
    }

    @Test
    void shouldReturnFalseWhenRoleDoesNotExist() {
        Integer roleId = 999;
        Mono<Boolean> existsMono = Mono.just(false);

        when(roleReactiveRepository.existsById(roleId)).thenReturn(existsMono);
        when(transactionalOperator.transactional(existsMono)).thenReturn(existsMono);

        StepVerifier.create(roleReactiveRepositoryAdapter.existsById(roleId))
                .expectNext(false)
                .verifyComplete();

        verify(roleReactiveRepository).existsById(roleId);
    }

    @Test
    void shouldHandleNullRoleId() {
        Integer roleId = null;
        Mono<Boolean> existsMono = Mono.just(false);

        when(roleReactiveRepository.existsById(roleId)).thenReturn(existsMono);
        when(transactionalOperator.transactional(existsMono)).thenReturn(existsMono);

        StepVerifier.create(roleReactiveRepositoryAdapter.existsById(roleId))
                .expectNext(false)
                .verifyComplete();

        verify(roleReactiveRepository).existsById(roleId);
    }

    @Test
    void shouldApplyTransactionalOperator() {
        Integer roleId = 1;
        Mono<Boolean> existsMono = Mono.just(true);

        when(roleReactiveRepository.existsById(roleId)).thenReturn(existsMono);
        when(transactionalOperator.transactional(existsMono)).thenReturn(existsMono);

        Mono<Boolean> result = roleReactiveRepositoryAdapter.existsById(roleId);

        assertNotNull(result);
        verify(roleReactiveRepository).existsById(roleId);
    }
}