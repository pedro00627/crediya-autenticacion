package co.com.pragma.api.security;

import co.com.pragma.api.utils.JWTUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JWTAuthenticationFilter implements WebFilter {

    private final JWTUtil jwtUtil;

    public JWTAuthenticationFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPathExcluded(path)) {
            return chain.filter(exchange);
        }

        return Mono.justOrEmpty(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .flatMap(this::validateAndExtractUsername)
                .flatMap(username -> {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))  // Ajusta roles según tu caso
                    );
                    return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    })
                .switchIfEmpty(Mono.error(new RuntimeException("Missing or invalid Authorization header")))
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isPathExcluded(String path) {
        return path.contains("/auth") || path.contains("/swagger") || path.contains("/webjars") || path.contains("/v3/api-docs");
    }

    private Mono<String> validateAndExtractUsername(String authToken) {
        return Mono.fromCallable(() -> {
            String username = jwtUtil.extractUsername(authToken);
            if (jwtUtil.validateToken(authToken, username)) {
                return username;
            } else {
                throw new RuntimeException("Invalid JWT token");
            }
        }).onErrorResume(e -> Mono.error(new RuntimeException("JWT validation failed", e)));
    }
}
