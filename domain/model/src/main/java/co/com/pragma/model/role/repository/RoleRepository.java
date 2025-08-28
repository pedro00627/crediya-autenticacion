package co.com.pragma.model.role.repository;

import reactor.core.publisher.Mono;

public interface RoleRepository {
    Mono<Boolean> existsById(Integer id);
}