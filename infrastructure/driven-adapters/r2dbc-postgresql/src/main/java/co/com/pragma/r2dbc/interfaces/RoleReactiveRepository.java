package co.com.pragma.r2dbc.interfaces;

import co.com.pragma.r2dbc.entity.RoleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RoleReactiveRepository extends ReactiveCrudRepository<RoleEntity, Integer> {
}