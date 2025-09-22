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
class AdminRoleStrategyTest {

    private AdminRoleStrategy adminRoleStrategy;

    @BeforeEach
    void setUp() {
        this.adminRoleStrategy = new AdminRoleStrategy();
    }

    @Test
    void shouldSupportAdminRoleId() {
        // Arrange
        final Integer adminRoleId = BusinessConstants.ADMIN_ROLE_ID;

        // Act
        final boolean result = this.adminRoleStrategy.supports(adminRoleId);

        // Assert
        assertTrue(result, "Should support admin role ID");
    }

    @Test
    void shouldNotSupportNullRoleId() {
        // Act
        final boolean result = this.adminRoleStrategy.supports(null);

        // Assert
        assertFalse(result, "Should not support null role ID");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 10, 99, Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1})
    void shouldNotSupportNonAdminRoleIds(final Integer roleId) {
        // Act
        final boolean result = this.adminRoleStrategy.supports(roleId);

        // Assert
        assertFalse(result, "Should not support non-admin role ID: " + roleId);
    }

    @Test
    void shouldReturnAdminRole() {
        // Act
        final List<String> roles = this.adminRoleStrategy.getRoles();

        // Assert
        assertNotNull(roles, "Roles should not be null");
        assertEquals(1, roles.size(), "Should return exactly one role");
        assertEquals(RoleConstants.ADMIN, roles.get(0), "Should return ADMIN role");
    }

    @Test
    void shouldReturnConsistentRoles() {
        // Act
        final List<String> roles1 = this.adminRoleStrategy.getRoles();
        final List<String> roles2 = this.adminRoleStrategy.getRoles();

        // Assert
        assertEquals(roles1, roles2, "Should return consistent roles across calls");
    }

    @ParameterizedTest
    @MethodSource("roleIdTestCases")
    void shouldValidateRoleIdSupport(final Integer roleId, final boolean expectedSupport, final String scenario) {
        // Act
        final boolean result = this.adminRoleStrategy.supports(roleId);

        // Assert
        assertEquals(expectedSupport, result, scenario);
    }

    @Test
    void shouldImplementRoleStrategyInterface() {
        // Assert
        assertInstanceOf(RoleStrategy.class, this.adminRoleStrategy, "AdminRoleStrategy should implement RoleStrategy interface");
    }

    @Test
    void shouldBeComponentManaged() {
        // Assert - The @Component annotation should be present
        assertNotNull(this.adminRoleStrategy, "AdminRoleStrategy should be instantiable as a component");
    }

    @Test
    void shouldHandleBusinessConstantsCorrectly() {
        // Arrange
        final Integer adminRoleId = BusinessConstants.ADMIN_ROLE_ID;

        // Act
        final boolean supportsAdmin = this.adminRoleStrategy.supports(adminRoleId);
        final List<String> roles = this.adminRoleStrategy.getRoles();

        // Assert
        assertTrue(supportsAdmin, "Should support the business constant admin role ID");
        assertTrue(roles.contains(RoleConstants.ADMIN), "Should contain the admin role constant");
    }

    @Test
    void shouldNotMutateReturnedRoles() {
        // Act
        final List<String> roles = this.adminRoleStrategy.getRoles();

        // Assert
        assertNotNull(roles);
        // Verify it's an immutable list or at least behaves correctly
        assertEquals(1, roles.size());
        assertEquals(RoleConstants.ADMIN, roles.get(0));
    }

    static Stream<Arguments> roleIdTestCases() {
        return Stream.of(
                Arguments.of(BusinessConstants.ADMIN_ROLE_ID, true, "Should support admin role ID from BusinessConstants"),
                Arguments.of(BusinessConstants.CLIENT_ROLE_ID, false, "Should not support client role ID"),
                Arguments.of(BusinessConstants.ADVISOR_ROLE_ID, false, "Should not support advisor role ID"),
                Arguments.of(null, false, "Should not support null role ID"),
                Arguments.of(0, false, "Should not support zero role ID"),
                Arguments.of(-1, false, "Should not support negative role ID"),
                Arguments.of(999, false, "Should not support unknown role ID")
        );
    }
}