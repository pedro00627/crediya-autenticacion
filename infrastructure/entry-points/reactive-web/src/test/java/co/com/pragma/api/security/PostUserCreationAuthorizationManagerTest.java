package co.com.pragma.api.security;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.security.model.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostUserCreationAuthorizationManagerTest {

    @Mock
    private LoggerPort logger;

    private PostUserCreationAuthorizationManager authorizationManager;

    @BeforeEach
    void setUp() {
        this.authorizationManager = new PostUserCreationAuthorizationManager(this.logger);
    }

    @Test
    void shouldGrantAccessToAdminUser() {
        // Arrange
        final Authentication auth = createAuthenticationWithRoles(List.of("ROLE_ADMIN"));
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(true, decision.isGranted()))
                .verifyComplete();
    }

    @Test
    void shouldGrantAccessToAdvisorUser() {
        // Arrange
        final Authentication auth = createAuthenticationWithRoles(List.of("ROLE_ADVISOR"));
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(true, decision.isGranted()))
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessToClientUser() {
        // Arrange
        final Authentication auth = createAuthenticationWithRoles(List.of("ROLE_CLIENT"));
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(false, decision.isGranted()))
                .verifyComplete();
    }

    @Test
    void shouldGrantAccessToUserWithMultipleValidRoles() {
        // Arrange
        final Authentication auth = createAuthenticationWithRoles(List.of("ROLE_ADMIN", "ROLE_CLIENT"));
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(true, decision.isGranted()))
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessForUnauthenticatedUser() {
        // Arrange
        final Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(false, decision.isGranted()))
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessForEmptyAuthentication() {
        // Arrange
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.empty(), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(false, decision.isGranted()))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("roleAuthorizationTestCases")
    void shouldHandleRoleBasedAuthorization(final List<String> roles, final boolean expectedAccess, final String scenario) {
        // Arrange
        final Authentication auth = createAuthenticationWithRoles(roles);
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(expectedAccess, decision.isGranted(), scenario))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("roleNormalizationTestCases")
    void shouldNormalizeRolesCorrectly(final List<String> rawRoles, final boolean expectedAccess, final String scenario) {
        // Arrange
        final Authentication auth = createAuthenticationWithRoles(rawRoles);
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(expectedAccess, decision.isGranted(), scenario))
                .verifyComplete();
    }

    @Test
    void shouldReturnAuthorizationResultFromAuthorizeMethod() {
        // Arrange
        final Authentication auth = createAuthenticationWithRoles(List.of("ROLE_ADMIN"));
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationResult> result = this.authorizationManager.authorize(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(authResult -> {
                    assertNotNull(authResult);
                    assertInstanceOf(AuthorizationDecision.class, authResult);
                    assertEquals(true, authResult.isGranted());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleUserWithUnknownRole() {
        // Arrange
        final Authentication auth = createAuthenticationWithRoles(List.of("ROLE_UNKNOWN"));
        final AuthorizationContext context = createAuthorizationContext();

        // Act
        final Mono<AuthorizationDecision> result = this.authorizationManager.check(Mono.just(auth), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> assertEquals(false, decision.isGranted()))
                .verifyComplete();
    }

    @Test
    void shouldBeAnnotatedAsComponent() {
        // Arrange & Act
        final boolean isComponent = this.authorizationManager.getClass().isAnnotationPresent(org.springframework.stereotype.Component.class);

        // Assert
        assertEquals(true, isComponent, "PostUserCreationAuthorizationManager should be annotated with @Component");
    }

    static Stream<Arguments> roleAuthorizationTestCases() {
        return Stream.of(
                Arguments.of(List.of("ROLE_ADMIN"), true, "Admin should have access"),
                Arguments.of(List.of("ROLE_ADVISOR"), true, "Advisor should have access"),
                Arguments.of(List.of("ROLE_CLIENT"), false, "Client should not have access"),
                Arguments.of(List.of("ROLE_ADMIN", "ROLE_CLIENT"), true, "User with admin and client roles should have access"),
                Arguments.of(List.of("ROLE_ADVISOR", "ROLE_CLIENT"), true, "User with advisor and client roles should have access"),
                Arguments.of(List.of("ROLE_CLIENT", "ROLE_USER"), false, "User with only non-admin roles should not have access"),
                Arguments.of(List.of(), false, "User with no roles should not have access")
        );
    }

    static Stream<Arguments> roleNormalizationTestCases() {
        return Stream.of(
                Arguments.of(List.of("ADMIN"), true, "Should handle normalized admin role"),
                Arguments.of(List.of("ADVISOR"), true, "Should handle normalized advisor role"),
                Arguments.of(List.of("CLIENT"), false, "Should handle normalized client role"),
                Arguments.of(List.of("ROLE_ADMIN"), true, "Should handle prefixed admin role"),
                Arguments.of(List.of("ROLE_ADVISOR"), true, "Should handle prefixed advisor role"),
                Arguments.of(List.of("ROLE_CLIENT"), false, "Should handle prefixed client role"),
                Arguments.of(List.of("ADMIN", "ROLE_CLIENT"), true, "Should handle mixed normalized and prefixed roles"),
                Arguments.of(List.of("ROLE_ADVISOR", "CLIENT"), true, "Should handle mixed prefixed and normalized roles")
        );
    }

    @SuppressWarnings("unchecked")
    private Authentication createAuthenticationWithRoles(final List<String> roles) {
        final Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("testuser@example.com");

        final Collection<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(authority -> (GrantedAuthority) authority)
                .toList();

        when(auth.getAuthorities()).thenReturn((Collection) authorities);
        return auth;
    }

    private AuthorizationContext createAuthorizationContext() {
        final MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/usuarios").build();
        final MockServerWebExchange exchange = MockServerWebExchange.from(request);
        return new AuthorizationContext(exchange);
    }
}