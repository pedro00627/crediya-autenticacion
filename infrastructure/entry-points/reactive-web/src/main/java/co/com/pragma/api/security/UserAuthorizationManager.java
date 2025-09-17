package co.com.pragma.api.security;

import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * Adaptador que integra la lógica de autorización de negocio con las interfaces de Spring Security.
 * Esta clase es el único punto que depende directamente de ReactiveAuthorizationManager,
 * aislando la lógica de negocio de las particularidades del framework.
 */
public class UserAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final UserAuthorizationLogic authorizationLogic;
    private final LoggerPort logger; // Inyectar LoggerPort

    /**
     * Constructor para UserAuthorizationManager.
     * Inicializa la lógica de autorización de usuario.
     *
     * @param logger El puerto de logging para registrar eventos.
     */
    public UserAuthorizationManager(LoggerPort logger) {
        this.logger = logger;
        this.authorizationLogic = new UserAuthorizationLogic(logger);
    }

    /**
     * Realiza la verificación de autorización para un contexto dado.
     * Delega la lógica de negocio a {@link UserAuthorizationLogic}.
     *
     * @param authentication Un {@link Mono} que emite la información de autenticación del usuario.
     * @param context        El contexto de autorización.
     * @return Un {@link Mono} que emite un {@link AuthorizationDecision} indicando si la autorización es concedida o denegada.
     */
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        logger.debug("UserAuthorizationManager: Delegating check to UserAuthorizationLogic for context: {}", context.getExchange().getRequest().getPath().value());
        return this.authorizationLogic.check(authentication, context);
    }
}
