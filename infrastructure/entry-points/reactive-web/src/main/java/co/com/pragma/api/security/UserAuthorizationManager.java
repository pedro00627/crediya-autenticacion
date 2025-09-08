package co.com.pragma.api.security;

import co.com.pragma.model.security.RoleConstants;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
                .map(auth -> {
                    // Regla 1: Si es ADMIN o ADVISOR, tiene acceso.
                    boolean isAdminOrAdvisor = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(role -> role.equals(RoleConstants.ROLE_ADMIN) || role.equals(RoleConstants.ROLE_ADVISOR));

                    if (isAdminOrAdvisor) {
                        return new AuthorizationDecision(true);
                    }

                    // Regla 2: Si es CLIENT, solo puede acceder a su propia información.
                    String requestedEmail = context.getExchange().getRequest().getQueryParams().getFirst("email");
                    String authenticatedUserEmail = auth.getName();

                    boolean isOwner = authenticatedUserEmail.equals(requestedEmail);

                    return new AuthorizationDecision(isOwner);
                })
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
