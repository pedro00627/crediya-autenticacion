package co.com.pragma.api.config;

import co.com.pragma.api.security.UserAuthorizationManager;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.security.api.JWTAuthenticationFilter;
import co.com.pragma.security.api.config.CommonSecurityConfig;
import co.com.pragma.security.api.config.SecurityFilterChainBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties({SecurityProperties.class, SecurityRulesProperties.class})
@Import(CommonSecurityConfig.class)
public class SecurityConfig {

    private final LoggerPort logger;
    private final SecurityFilterChainBuilder securityFilterChainBuilder;

    public SecurityConfig(final LoggerPort logger,
                          final SecurityFilterChainBuilder securityFilterChainBuilder) {
        this.logger = logger;
        this.securityFilterChainBuilder = securityFilterChainBuilder;
    }

    @Bean
    @Primary
    public UserAuthorizationManager userAuthorizationManager(final LoggerPort logger) {
        return new UserAuthorizationManager(logger);
    }

    @Bean
    @Primary
    public JWTAuthenticationFilter jwtAuthenticationFilter(final co.com.pragma.security.util.JWTUtil jwtUtil,
                                                           final LoggerPort logger,
                                                           final SecurityProperties securityProperties) {
        return new JWTAuthenticationFilter(jwtUtil, logger, securityProperties);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http,
                                                         final SecurityProperties securityProperties,
                                                         final SecurityRulesProperties securityRulesProperties,
                                                         final JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.logger.info("Configuring SecurityWebFilterChain for Autenticacion microservice...");

        // Create a matcher for the paths that should be excluded from security.
        final ServerWebExchangeMatcher excludedPathsMatcher = ServerWebExchangeMatchers.pathMatchers(
                securityProperties.excludedPaths().toArray(new String[0])
        );

        // The security filter chain will only apply to paths that are NOT in the exclusion list.
        final ServerWebExchangeMatcher securedPathsMatcher = new NegatedServerWebExchangeMatcher(excludedPathsMatcher);

        return http
                .securityMatcher(securedPathsMatcher)
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN)))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchanges ->
                        this.securityFilterChainBuilder.applyAuthorizationRules(exchanges, securityRulesProperties, securityProperties)
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
