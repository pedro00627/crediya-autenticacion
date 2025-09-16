package co.com.pragma.api.security;

import co.com.pragma.api.utils.JWTUtil;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTAuthenticationFilter implements WebFilter {

    private static final String[] EXCLUDED_PATHS = {"/api/v1/login", "/swagger", "/webjars", "/v3/api-docs"};
    private final JWTUtil jwtUtil;
    private final LoggerPort logger;

    public JWTAuthenticationFilter(JWTUtil jwtUtil, LoggerPort logger) {
        this.jwtUtil = jwtUtil;
        this.logger = logger;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 1. Si la ruta está excluida, simplemente continúa la cadena de filtros sin autenticación.
        if (isPathExcluded(path)) {
            return chain.filter(exchange);
        }

        // 2. Intenta autenticar y proceder con la cadena de filtros.
        // Los errores de token inválido (InvalidTokenException) o inesperados serán propagados.
        // Si no hay token o no es Bearer, authenticateAndProceed devolverá Mono.empty().
        return authenticateAndProceed(exchange, chain, path)
                .onErrorResume(InvalidTokenException.class, e -> {
                    // 3. Si la validación del token falla (InvalidTokenException), registra un WARN y deniega el acceso.
                    logger.warn("Authorization failed for request to {}. Invalid JWT token provided: {}", path, e.getReason());
                    return setUnauthorizedResponse(exchange);
                })
                .onErrorResume(e -> {
                    // 4. Para cualquier otra excepción inesperada durante la autenticación, registra un ERROR y deniega el acceso.
                    logger.error("Unexpected error during authentication for request to {}: {}", e);
                    return setUnauthorizedResponse(exchange);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // 5. Este switchIfEmpty se activa si authenticateAndProceed devuelve Mono.empty()
                    // (es decir, no se encontró token o no era Bearer).
                    // En este caso, no se genera un log, solo se deniega el acceso.
                    return setUnauthorizedResponse(exchange);
                }))
                .then(); // Asegura que el tipo de retorno final sea Mono<Void>
    }

    /**
     * Intenta autenticar la solicitud y procede con la cadena de filtros si es exitoso.
     * - Devuelve Mono.empty() si no hay token de autorización o no es un token Bearer.
     * - Propaga Mono.error(InvalidTokenException) si el token es inválido.
     * - Devuelve Mono<Void> si la autenticación es exitosa y la cadena de filtros procede.
     *
     * @param exchange El ServerWebExchange.
     * @param chain La cadena de filtros web.
     * @param path La ruta de la solicitud.
     * @return Un Mono<Void> que representa la finalización del procesamiento.
     */
    private Mono<Void> authenticateAndProceed(ServerWebExchange exchange, WebFilterChain chain, String path) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer ")) // Filtra si no es un token Bearer
                .map(authHeader -> authHeader.substring(7)) // Extrae el token
                .flatMap(token -> // Si hay un token Bearer, intenta validarlo
                        validateAndCreateAuthentication(token)
                                .flatMap(authentication -> chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                );
    }

    /**
     * Método auxiliar para establecer el estado HTTP en UNAUTHORIZED y completar la respuesta.
     * @param exchange El ServerWebExchange.
     * @return Un Mono<Void> que indica la finalización.
     */
    private Mono<Void> setUnauthorizedResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private boolean isPathExcluded(String path) {
        return Arrays.stream(EXCLUDED_PATHS).anyMatch(path::contains);
    }

    private Mono<Authentication> validateAndCreateAuthentication(String token) {
        return Mono.fromCallable(() -> {
            String username = jwtUtil.extractUsername(token);
            List<String> roles = jwtUtil.extractRoles(token);

            if (username == null || roles == null || roles.isEmpty()) {
                throw new InvalidTokenException("Missing username or roles in JWT token");
            }

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return (Authentication) new UsernamePasswordAuthenticationToken(username, null, authorities);
        })
        .onErrorResume(e -> Mono.error(new InvalidTokenException("Error processing JWT token", e)));
    }

    // Excepción personalizada para diferenciar entre token faltante y token inválido
    private static class InvalidTokenException extends ResponseStatusException {
        public InvalidTokenException(String reason) {
            super(HttpStatus.UNAUTHORIZED, reason);
        }
        public InvalidTokenException(String reason, Throwable cause) {
            super(HttpStatus.UNAUTHORIZED, reason, cause);
        }
    }
}
