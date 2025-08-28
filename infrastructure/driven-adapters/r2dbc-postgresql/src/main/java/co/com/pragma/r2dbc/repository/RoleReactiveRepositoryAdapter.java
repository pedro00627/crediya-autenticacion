package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.role.repository.RoleRepository;
import co.com.pragma.r2dbc.interfaces.RoleReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RoleReactiveRepositoryAdapter implements RoleRepository {

    private final RoleReactiveRepository repository;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Boolean> existsById(Integer id) {

        return repository.existsById(id).as(transactionalOperator::transactional);
    }
}