package co.com.pragma.api.config;

import co.com.pragma.api.security.JWTAuthenticationFilter;
import co.com.pragma.api.security.UserAuthorizationManager;
import co.com.pragma.model.security.RoleConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_API_PATHS = {
            "/api/v1/login**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/v3/api-docs/**"
    };
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final UserAuthorizationManager userAuthorizationManager;

    public SecurityConfig(JWTAuthenticationFilter jwtAuthenticationFilter, UserAuthorizationManager userAuthorizationManager) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userAuthorizationManager = userAuthorizationManager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .exceptionHandling(spec -> spec.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_API_PATHS).permitAll()
                        // Regla para el endpoint POST de usuarios
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios").hasAnyRole(RoleConstants.ADMIN, RoleConstants.ADVISOR)
                        // Reglas de la historia de usuario
                        .pathMatchers(HttpMethod.POST, "/api/users").hasAnyRole(RoleConstants.ADMIN, RoleConstants.ADVISOR)
                        .pathMatchers(HttpMethod.POST, "/api/loans").hasRole(RoleConstants.CLIENT)
                        // Regla personalizada para obtener usuarios
                        .pathMatchers(HttpMethod.GET, "/api/v1/usuarios").access(userAuthorizationManager)
                        // Para el resto de rutas, solo se necesita estar autenticado
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
