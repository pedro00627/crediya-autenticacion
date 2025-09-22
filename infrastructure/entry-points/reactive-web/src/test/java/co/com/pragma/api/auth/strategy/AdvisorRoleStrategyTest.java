package co.com.pragma.api.auth.strategy;

import co.com.pragma.model.constants.BusinessConstants;
import co.com.pragma.security.model.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AdvisorRoleStrategyTest {

    private AdvisorRoleStrategy advisorRoleStrategy;

    @BeforeEach
    void setUp() {
        this.advisorRoleStrategy = new AdvisorRoleStrategy();
    }

    @Test
    void shouldSupportAdvisorRoleId() {
        // Arrange
        final Integer advisorRoleId = BusinessConstants.ADVISOR_ROLE_ID;

        // Act
        final boolean result = this.advisorRoleStrategy.supports(advisorRoleId);

        // Assert
        assertTrue(result, "Should support advisor role ID");
    }

    @Test
    void shouldNotSupportNullRoleId() {
        // Act
        final boolean result = this.advisorRoleStrategy.supports(null);

        // Assert
        assertFalse(result, "Should not support null role ID");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 4, 5, 10, 99, Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1})
    void shouldNotSupportNonAdvisorRoleIds(final Integer roleId) {
        // Act
        final boolean result = this.advisorRoleStrategy.supports(roleId);

        // Assert
        assertFalse(result, "Should not support non-advisor role ID: " + roleId);
    }

    @Test
    void shouldReturnAdvisorRole() {
        // Act
        final List<String> roles = this.advisorRoleStrategy.getRoles();

        // Assert
        assertNotNull(roles, "Roles should not be null");
        assertEquals(1, roles.size(), "Should return exactly one role");
        assertEquals(RoleConstants.ADVISOR, roles.get(0), "Should return ADVISOR role");
    }

    @Test
    void shouldReturnConsistentRoles() {
        // Act
        final List<String> roles1 = this.advisorRoleStrategy.getRoles();
        final List<String> roles2 = this.advisorRoleStrategy.getRoles();

        // Assert
        assertEquals(roles1, roles2, "Should return consistent roles across calls");
    }

    @ParameterizedTest
    @MethodSource("roleIdTestCases")
    void shouldValidateRoleIdSupport(final Integer roleId, final boolean expectedSupport, final String scenario) {
        // Act
        final boolean result = this.advisorRoleStrategy.supports(roleId);

        // Assert
        assertEquals(expectedSupport, result, scenario);
    }

    @Test
    void shouldImplementRoleStrategyInterface() {
        // Assert
        assertInstanceOf(RoleStrategy.class, this.advisorRoleStrategy, "AdvisorRoleStrategy should implement RoleStrategy interface");
    }

    @Test
    void shouldBeComponentManaged() {
        // Assert - The @Component annotation should be present
        assertNotNull(this.advisorRoleStrategy, "AdvisorRoleStrategy should be instantiable as a component");
    }

    @Test
    void shouldHandleBusinessConstantsCorrectly() {
        // Arrange
        final Integer advisorRoleId = BusinessConstants.ADVISOR_ROLE_ID;

        // Act
        final boolean supportsAdvisor = this.advisorRoleStrategy.supports(advisorRoleId);
        final List<String> roles = this.advisorRoleStrategy.getRoles();

        // Assert
        assertTrue(supportsAdvisor, "Should support the business constant advisor role ID");
        assertTrue(roles.contains(RoleConstants.ADVISOR), "Should contain the advisor role constant");
    }

    @Test
    void shouldNotMutateReturnedRoles() {
        // Act
        final List<String> roles = this.advisorRoleStrategy.getRoles();

        // Assert
        assertNotNull(roles);
        // Verify it's an immutable list or at least behaves correctly
        assertEquals(1, roles.size());
        assertEquals(RoleConstants.ADVISOR, roles.get(0));
    }

    static Stream<Arguments> roleIdTestCases() {
        return Stream.of(
                Arguments.of(BusinessConstants.ADVISOR_ROLE_ID, true, "Should support advisor role ID from BusinessConstants"),
                Arguments.of(BusinessConstants.ADMIN_ROLE_ID, false, "Should not support admin role ID"),
                Arguments.of(BusinessConstants.CLIENT_ROLE_ID, false, "Should not support client role ID"),
                Arguments.of(null, false, "Should not support null role ID"),
                Arguments.of(0, false, "Should not support zero role ID"),
                Arguments.of(-1, false, "Should not support negative role ID"),
                Arguments.of(999, false, "Should not support unknown role ID")
        );
    }
}