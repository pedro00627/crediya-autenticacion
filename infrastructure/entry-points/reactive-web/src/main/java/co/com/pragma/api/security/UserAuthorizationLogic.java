package co.com.pragma.api.security;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.security.model.RoleConstants;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Contiene la lógica de negocio pura para la autorización de usuarios,
 * desacoplada de las interfaces de Spring Security.
 */
public class UserAuthorizationLogic {

    private static final Set<String> REQUIRED_ROLES_ADMIN_ADVISOR = Set.of(RoleConstants.ADMIN, RoleConstants.ADVISOR);
    private static final String CLIENT_ROLE = RoleConstants.CLIENT;
    private final LoggerPort logger;

    /**
     * Constructor para UserAuthorizationLogic.
     *
     * @param logger El puerto de logging para registrar eventos.
     */
    public UserAuthorizationLogic(LoggerPort logger) {
        this.logger = logger;
    }

    /**
     * Realiza la verificación de autorización basada en la autenticación del usuario, los roles requeridos
     * y el contexto de la solicitud.
     *
     * @param authentication Un {@link Mono} que emite la información de autenticación del usuario.
     * @param context        El contexto de autorización, que contiene la solicitud.
     * @return Un {@link Mono} que emite un {@link AuthorizationDecision} indicando si la autorización es concedida o denegada.
     */
    public Mono<AuthorizationDecision> authorize(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
                .doOnNext(auth -> logger.debug("UserAuthorizationLogic: Authorizing authentication for user: {} with authorities: {}", auth.getName(), auth.getAuthorities()))
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    Set<String> userRoles = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(java.util.stream.Collectors.toUnmodifiableSet());

                    // Normalizar roles removiendo el prefijo ROLE_ para comparación
                    Set<String> normalizedRoles = userRoles.stream()
                            .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                            .collect(java.util.stream.Collectors.toUnmodifiableSet());

                    logger.debug("UserAuthorizationLogic: User {} has roles: {} (normalized: {})", auth.getName(), userRoles, normalizedRoles);

                    // Regla 1: Si es ADMIN o ADVISOR, tiene acceso.
                    if (normalizedRoles.stream().anyMatch(REQUIRED_ROLES_ADMIN_ADVISOR::contains)) {
                        logger.debug("UserAuthorizationLogic: User {} has ADMIN/ADVISOR role. Access granted.", auth.getName());
                        return new AuthorizationDecision(true);
                    }

                    // Regla 2: Si es CLIENT, solo puede acceder a su propia información.
                    if (normalizedRoles.contains(CLIENT_ROLE)) {
                        String requestedEmail = context.getExchange().getRequest().getQueryParams().getFirst("email");
                        String authenticatedUserEmail = auth.getName(); // El nombre del usuario autenticado es el email

                        logger.debug("UserAuthorizationLogic: Client user {}. Requested email: {}. Authenticated email: {}", authenticatedUserEmail, requestedEmail, authenticatedUserEmail);

                        boolean isOwner = authenticatedUserEmail.equalsIgnoreCase(requestedEmail);
                        if (isOwner) {
                            logger.debug("UserAuthorizationLogic: Client user {} is requesting their own data. Access granted.", authenticatedUserEmail);
                            return new AuthorizationDecision(true);
                        } else {
                            logger.warn("UserAuthorizationLogic: Client user {} is requesting data for {}. Access denied.", authenticatedUserEmail, requestedEmail);
                            return new AuthorizationDecision(false);
                        }
                    }

                    // Si no es ADMIN/ADVISOR ni CLIENT, denegar por defecto.
                    logger.warn("UserAuthorizationLogic: User {} has no recognized roles for this resource. Access denied.", auth.getName());
                    return new AuthorizationDecision(false);
                })
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
