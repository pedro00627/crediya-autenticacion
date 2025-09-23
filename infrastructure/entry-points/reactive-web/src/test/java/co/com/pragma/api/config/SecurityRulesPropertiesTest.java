package co.com.pragma.api.config;

import co.com.pragma.security.api.config.AuthorizationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityRulesPropertiesTest {

    private SecurityRulesProperties securityRulesProperties;

    @BeforeEach
    void setUp() {
        securityRulesProperties = new SecurityRulesProperties();
    }

    @Test
    void shouldInitializeWithEmptyAuthorization() {
        assertNotNull(this.securityRulesProperties.getAuthorization());
        assertTrue(this.securityRulesProperties.getAuthorization().isEmpty());
        assertInstanceOf(ArrayList.class, this.securityRulesProperties.getAuthorization());
    }

    @Test
    void shouldSetAndGetAuthorization() {
        List<AuthorizationRule> rules = List.of(
                mock(AuthorizationRule.class),
                mock(AuthorizationRule.class)
        );

        this.securityRulesProperties.setAuthorization(rules);

        assertEquals(rules, this.securityRulesProperties.getAuthorization());
        assertEquals(rules, this.securityRulesProperties.authorization());
    }

    @ParameterizedTest
    @MethodSource("authorizationRuleScenarios")
    void shouldHandleAuthorizationRules(String scenario, List<AuthorizationRule> rules, boolean shouldBeEmpty) {
        this.securityRulesProperties.setAuthorization(rules);

        List<AuthorizationRule> result = this.securityRulesProperties.getAuthorization();
        List<AuthorizationRule> interfaceResult = this.securityRulesProperties.authorization();

        assertNotNull(result, scenario);
        assertEquals(result, interfaceResult, scenario + " - Interface should return same as getter");
        assertEquals(shouldBeEmpty, result.isEmpty(), scenario);
        assertEquals(rules.size(), result.size(), scenario);
    }

    @Test
    void shouldImplementSecurityRulesProviderInterface() {
        List<AuthorizationRule> testRules = List.of(
                mock(AuthorizationRule.class),
                mock(AuthorizationRule.class),
                mock(AuthorizationRule.class)
        );

        this.securityRulesProperties.setAuthorization(testRules);

        // Verify interface method works correctly
        assertEquals(testRules, this.securityRulesProperties.authorization());
        assertSame(this.securityRulesProperties.getAuthorization(), this.securityRulesProperties.authorization());
    }

    @Test
    void shouldMaintainReferenceConsistency() {
        List<AuthorizationRule> rules = new ArrayList<>();
        rules.add(mock(AuthorizationRule.class));

        this.securityRulesProperties.setAuthorization(rules);

        List<AuthorizationRule> retrievedRules = this.securityRulesProperties.getAuthorization();

        assertSame(rules, retrievedRules, "Should maintain reference to the same list");
    }

    static Stream<Arguments> authorizationRuleScenarios() {
        return Stream.of(
            Arguments.of("Empty rules list",
                        List.of(), true),
            Arguments.of("Single authorization rule",
                        List.of(mock(AuthorizationRule.class)), false),
            Arguments.of("Multiple authorization rules",
                        List.of(mock(AuthorizationRule.class), mock(AuthorizationRule.class)), false),
            Arguments.of("Complex authorization setup",
                        List.of(mock(AuthorizationRule.class), mock(AuthorizationRule.class),
                               mock(AuthorizationRule.class), mock(AuthorizationRule.class)), false)
        );
    }
}