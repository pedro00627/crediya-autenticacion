package co.com.pragma.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SecurityPropertiesTest {

    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
    }

    @Test
    void shouldInitializeWithDefaults() {
        assertNull(this.securityProperties.getSecret());
        assertEquals(0L, this.securityProperties.getExpiration());
        assertNotNull(this.securityProperties.getExcludedPaths());
        assertTrue(this.securityProperties.getExcludedPaths().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"jwt-secret-key", "dev-secret", "prod-secret-2024", ""})
    void shouldSetAndGetSecret(String secret) {
        this.securityProperties.setSecret(secret);

        assertEquals(secret, this.securityProperties.getSecret());
        assertEquals(secret, this.securityProperties.secret());
    }

    @ParameterizedTest
    @ValueSource(longs = {3600000L, 7200000L, 86400000L, 0L, -1L})
    void shouldSetAndGetExpiration(long expiration) {
        this.securityProperties.setExpiration(expiration);

        assertEquals(expiration, this.securityProperties.getExpiration());
        assertEquals(expiration, this.securityProperties.expiration());
    }

    @ParameterizedTest
    @MethodSource("excludedPathsScenarios")
    void shouldSetAndGetExcludedPaths(String scenario, List<String> paths, boolean shouldBeEmpty) {
        this.securityProperties.setExcludedPaths(paths);

        List<String> result = this.securityProperties.getExcludedPaths();
        List<String> interfaceResult = this.securityProperties.excludedPaths();

        assertNotNull(result, scenario);
        assertEquals(result, interfaceResult, scenario + " - Interface should return same as getter");
        assertEquals(shouldBeEmpty, result.isEmpty(), scenario);
    }

    @Test
    void shouldHandleNullExcludedPaths() {
        this.securityProperties.setExcludedPaths(null);

        List<String> result = this.securityProperties.getExcludedPaths();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertInstanceOf(ArrayList.class, result);
    }

    @Test
    void shouldImplementJWTPropertiesInterface() {
        final String testSecret = "test-secret";
        final long testExpiration = 3600000L;
        List<String> testPaths = List.of("/auth/login", "/health");

        this.securityProperties.setSecret(testSecret);
        this.securityProperties.setExpiration(testExpiration);
        this.securityProperties.setExcludedPaths(testPaths);

        // Verify interface methods work correctly
        assertEquals(testSecret, this.securityProperties.secret());
        assertEquals(testExpiration, this.securityProperties.expiration());
        assertEquals(testPaths, this.securityProperties.excludedPaths());
    }

    static Stream<Arguments> excludedPathsScenarios() {
        return Stream.of(
            Arguments.of("Business paths",
                        List.of("/auth/login", "/auth/register", "/actuator/health"), false),
            Arguments.of("Single path",
                        List.of("/public"), false),
            Arguments.of("Empty list",
                        List.of(), true),
            Arguments.of("Admin paths",
                        List.of("/admin/**", "/management/**"), false),
            Arguments.of("Health check paths",
                        List.of("/actuator/health", "/actuator/info"), false)
        );
    }
}