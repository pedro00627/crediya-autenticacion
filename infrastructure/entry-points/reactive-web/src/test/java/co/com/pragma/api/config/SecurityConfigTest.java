package co.com.pragma.api.config;

import co.com.pragma.api.security.UserAuthorizationManager;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.security.api.JWTAuthenticationFilter;
import co.com.pragma.security.api.config.SecurityFilterChainBuilder;
import co.com.pragma.security.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private LoggerPort logger;

    @Mock
    private SecurityFilterChainBuilder securityFilterChainBuilder;

    @Mock
    private JWTUtil jwtUtil;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(this.logger, this.securityFilterChainBuilder);
    }

    @Test
    void shouldCreateUserAuthorizationManager() {
        UserAuthorizationManager result = this.securityConfig.userAuthorizationManager(this.logger);

        assertNotNull(result);
        assertInstanceOf(UserAuthorizationManager.class, result);
    }

    @Test
    void shouldCreateJWTAuthenticationFilter() {
        SecurityProperties securityProperties = this.createTestSecurityProperties();

        JWTAuthenticationFilter result = this.securityConfig.jwtAuthenticationFilter(
                this.jwtUtil, this.logger, securityProperties);

        assertNotNull(result);
        assertInstanceOf(JWTAuthenticationFilter.class, result);
    }

    @Test
    void shouldCreateSecurityWebFilterChain() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        SecurityProperties securityProperties = this.createTestSecurityProperties();
        SecurityRulesProperties securityRulesProperties = this.createTestSecurityRulesProperties();
        JWTAuthenticationFilter jwtFilter = new JWTAuthenticationFilter(this.jwtUtil, this.logger, securityProperties);

        SecurityWebFilterChain result = this.securityConfig.securityWebFilterChain(
                http, securityProperties, securityRulesProperties, jwtFilter);

        assertNotNull(result);
        assertInstanceOf(SecurityWebFilterChain.class, result);
    }

    private SecurityProperties createTestSecurityProperties() {
        SecurityProperties properties = new SecurityProperties();
        properties.setSecret("test-secret");
        properties.setExpiration(3600000L);
        properties.setExcludedPaths(List.of("/auth/login", "/actuator/health"));
        return properties;
    }

    private SecurityRulesProperties createTestSecurityRulesProperties() {
        SecurityRulesProperties properties = new SecurityRulesProperties();
        properties.setAuthorization(List.of());
        return properties;
    }
}