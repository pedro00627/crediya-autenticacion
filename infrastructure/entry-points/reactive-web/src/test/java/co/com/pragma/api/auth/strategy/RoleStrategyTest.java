package co.com.pragma.api.auth.strategy;

import co.com.pragma.model.constants.BusinessConstants;
import co.com.pragma.security.model.RoleConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleStrategyTest {

    static Stream<Arguments> strategyTestCases() {
        return Stream.of(
                Arguments.of(new AdminRoleStrategy(), BusinessConstants.ADMIN_ROLE_ID),
                Arguments.of(new ClientRoleStrategy(), BusinessConstants.CLIENT_ROLE_ID),
                Arguments.of(new AdvisorRoleStrategy(), BusinessConstants.ADVISOR_ROLE_ID)
        );
    }

    static Stream<Arguments> strategyRoleTestCases() {
        return Stream.of(
                Arguments.of(new AdminRoleStrategy(), RoleConstants.ADMIN),
                Arguments.of(new ClientRoleStrategy(), RoleConstants.CLIENT),
                Arguments.of(new AdvisorRoleStrategy(), RoleConstants.ADVISOR)
        );
    }

    @ParameterizedTest
    @MethodSource("strategyTestCases")
    void shouldSupportCorrectRoleId(RoleStrategy strategy, Integer supportedRoleId) {
        assertTrue(strategy.supports(supportedRoleId));
    }

    @ParameterizedTest
    @MethodSource("strategyTestCases")
    void shouldReturnSingleRole(RoleStrategy strategy, Integer supportedRoleId) {
        List<String> roles = strategy.getRoles();
        assertEquals(1, roles.size());
    }

    @ParameterizedTest
    @MethodSource("strategyRoleTestCases")
    void shouldReturnCorrectRole(RoleStrategy strategy, String expectedRole) {
        List<String> roles = strategy.getRoles();
        assertEquals(expectedRole, roles.get(0));
    }

    @ParameterizedTest
    @MethodSource("strategyTestCases")
    void shouldNotSupportNull(RoleStrategy strategy, Integer supportedRoleId) {
        assertFalse(strategy.supports(null));
    }

    @ParameterizedTest
    @MethodSource("strategyTestCases")
    void shouldNotSupportUnknownRoleId(RoleStrategy strategy, Integer supportedRoleId) {
        assertFalse(strategy.supports(999));
        assertFalse(strategy.supports(0));
        assertFalse(strategy.supports(-1));
    }

    @Test
    void shouldMaintainConsistentRoles() {
        RoleStrategy strategy = new AdminRoleStrategy();
        List<String> roles1 = strategy.getRoles();
        List<String> roles2 = strategy.getRoles();

        assertEquals(roles1, roles2);
    }
}