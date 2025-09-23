package co.com.pragma.api.security;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.security.model.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthorizationLogicTest {

    @Mock
    private LoggerPort logger;

    private UserAuthorizationLogic authorizationLogic;

    @BeforeEach
    void setUp() {
        authorizationLogic = new UserAuthorizationLogic(logger);
    }

    @Test
    void shouldGrantAccessToAdminUser() {
        // Arrange
        Authentication authentication = createAuthentication("admin@example.com", true,
                List.of(new SimpleGrantedAuthority(RoleConstants.ADMIN)));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=client@example.com");

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertTrue(decision.isGranted(), "Admin should have access to any resource");
                })
                .verifyComplete();
    }

    @Test
    void shouldGrantAccessToAdvisorUser() {
        // Arrange
        Authentication authentication = createAuthentication("advisor@example.com", true,
                List.of(new SimpleGrantedAuthority(RoleConstants.ADVISOR)));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=client@example.com");

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertTrue(decision.isGranted(), "Advisor should have access to any resource");
                })
                .verifyComplete();
    }

    @Test
    void shouldGrantAccessToClientForOwnData() {
        // Arrange
        final String userEmail = "client@example.com";
        Authentication authentication = createAuthentication(userEmail, true,
                List.of(new SimpleGrantedAuthority(RoleConstants.CLIENT)));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=" + userEmail);

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertTrue(decision.isGranted(), "Client should have access to their own data");
                })
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessToClientForOtherUserData() {
        // Arrange
        final String authenticatedEmail = "client1@example.com";
        final String requestedEmail = "client2@example.com";
        Authentication authentication = createAuthentication(authenticatedEmail, true,
                List.of(new SimpleGrantedAuthority(RoleConstants.CLIENT)));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=" + requestedEmail);

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertFalse(decision.isGranted(), "Client should not have access to other user's data");
                })
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessForUnauthenticatedUser() {
        // Arrange
        Authentication authentication = createAuthentication("user@example.com", false,
                List.of(new SimpleGrantedAuthority(RoleConstants.CLIENT)));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=user@example.com");

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertFalse(decision.isGranted(), "Unauthenticated user should be denied access");
                })
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessForUserWithUnknownRole() {
        // Arrange
        Authentication authentication = createAuthentication("unknown@example.com", true,
                List.of(new SimpleGrantedAuthority("UNKNOWN_ROLE")));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=unknown@example.com");

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertFalse(decision.isGranted(), "User with unknown role should be denied access");
                })
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessForEmptyAuthentication() {
        // Arrange
        AuthorizationContext context = createAuthorizationContext("/api/users?email=test@example.com");

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.empty(), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertFalse(decision.isGranted());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_ADMIN", "ADMIN", "ROLE_ADVISOR", "ADVISOR"})
    void shouldGrantAccessForAdminOrAdvisorRoles(String roleAuthority) {
        // Arrange
        Authentication authentication = createAuthentication("privileged@example.com", true,
                List.of(new SimpleGrantedAuthority(roleAuthority)));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=anyone@example.com");

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertTrue(decision.isGranted(), "Should grant access for role: " + roleAuthority);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("clientAccessTestCases")
    void shouldHandleClientAccessCorrectly(String authenticatedEmail, String requestedEmail,
                                           boolean expectedAccess, String scenario) {
        // Arrange
        Authentication authentication = createAuthentication(authenticatedEmail, true,
                List.of(new SimpleGrantedAuthority(RoleConstants.CLIENT)));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=" + requestedEmail);

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision, scenario);
                    if (expectedAccess) {
                        assertTrue(decision.isGranted(), scenario);
                    } else {
                        assertFalse(decision.isGranted(), scenario);
                    }
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleMultipleRoles() {
        // Arrange - User with both CLIENT and ADMIN roles (ADMIN should take precedence)
        Authentication authentication = createAuthentication("multiRole@example.com", true,
                List.of(
                    new SimpleGrantedAuthority(RoleConstants.CLIENT),
                    new SimpleGrantedAuthority(RoleConstants.ADMIN)
                ));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=other@example.com");

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertTrue(decision.isGranted(), "User with ADMIN role should have access regardless of other roles");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleCaseInsensitiveEmailComparison() {
        // Arrange
        final String authenticatedEmail = "Client@Example.Com";
        final String requestedEmail = "client@example.com";
        Authentication authentication = createAuthentication(authenticatedEmail, true,
                List.of(new SimpleGrantedAuthority(RoleConstants.CLIENT)));
        AuthorizationContext context = createAuthorizationContext("/api/users?email=" + requestedEmail);

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertTrue(decision.isGranted(), "Email comparison should be case insensitive");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleRequestWithoutEmailParameter() {
        // Arrange
        Authentication authentication = createAuthentication("client@example.com", true,
                List.of(new SimpleGrantedAuthority(RoleConstants.CLIENT)));
        AuthorizationContext context = createAuthorizationContext("/api/users");

        // Act
        Mono<AuthorizationDecision> result = authorizationLogic.authorize(Mono.just(authentication), context);

        // Assert
        StepVerifier.create(result)
                .assertNext(decision -> {
                    assertNotNull(decision);
                    assertFalse(decision.isGranted(), "Client should be denied access when no email parameter is provided");
                })
                .verifyComplete();
    }

    // Helper methods
    @SuppressWarnings("unchecked")
    private Authentication createAuthentication(String name, boolean isAuthenticated,
                                                Collection<GrantedAuthority> authorities) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(name);
        when(authentication.isAuthenticated()).thenReturn(isAuthenticated);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        return authentication;
    }

    private AuthorizationContext createAuthorizationContext(String uri) {
        MockServerHttpRequest request = MockServerHttpRequest.get(uri).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AuthorizationContext context = mock(AuthorizationContext.class);
        lenient().when(context.getExchange()).thenReturn(exchange);
        return context;
    }

    static Stream<Arguments> clientAccessTestCases() {
        return Stream.of(
                Arguments.of("user@example.com", "user@example.com", true, "Client should access their own data"),
                Arguments.of("user@example.com", "other@example.com", false, "Client should not access other user's data"),
                Arguments.of("USER@EXAMPLE.COM", "user@example.com", true, "Should handle case insensitive email comparison"),
                Arguments.of("user@example.com", "USER@EXAMPLE.COM", true, "Should handle case insensitive requested email"),
                Arguments.of("user@example.com", "", false, "Client should not access when empty email is requested"),
                Arguments.of("user@example.com", null, false, "Client should not access when null email is requested")
        );
    }
}