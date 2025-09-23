package co.com.pragma.api.security;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.security.model.RoleConstants;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestor de autorización personalizado para la creación de usuarios (POST /api/v1/usuarios).
 * Requiere que el usuario autenticado tenga el rol ROLE_ADMIN o ROLE_ADVISOR.
 */
@Component("postUserCreationAuthorizationManager") // Nombre del bean para usar en application.yaml
public class PostUserCreationAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private static final Set<String> REQUIRED_ROLES = Set.of(RoleConstants.ADMIN, RoleConstants.ADVISOR);
    private final LoggerPort logger;

    public PostUserCreationAuthorizationManager(LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
        return authorize(authentication, object).cast(AuthorizationDecision.class);
    }

    @Override
    public Mono<AuthorizationResult> authorize(Mono<Authentication> authentication, AuthorizationContext object) {
        return authentication
                .doOnNext(auth -> logger.debug("PostUserCreationAuthorizationManager: Authorizing authentication for user: {} with authorities: {}", auth.getName(), auth.getAuthorities()))
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getAuthorities)
                .map(authorities -> authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toUnmodifiableSet()))
                .map(userRoles -> {
                    // Normalizar roles removiendo el prefijo ROLE_ para comparación
                    Set<String> normalizedRoles = userRoles.stream()
                            .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                            .collect(Collectors.toUnmodifiableSet());

                    logger.debug("PostUserCreationAuthorizationManager: User roles: {} (normalized: {}). Required roles: {}", userRoles, normalizedRoles, REQUIRED_ROLES);

                    return normalizedRoles.stream().anyMatch(REQUIRED_ROLES::contains);
                })
                .map(AuthorizationDecision::new)
                .doOnNext(decision -> logger.info("PostUserCreationAuthorizationManager: Authorization decision: {}", decision.isGranted()))
                .defaultIfEmpty(new AuthorizationDecision(false))
                .cast(AuthorizationResult.class);
    }
}
