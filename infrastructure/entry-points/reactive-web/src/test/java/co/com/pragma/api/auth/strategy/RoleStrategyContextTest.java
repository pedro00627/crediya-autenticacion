package co.com.pragma.api.auth.strategy;

import co.com.pragma.model.constants.BusinessConstants;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleStrategyContextTest {

    @Mock
    private RoleStrategy clientStrategy;

    @Mock
    private RoleStrategy advisorStrategy;

    @Mock
    private RoleStrategy adminStrategy;

    @Mock
    private LoggerPort logger;

    private RoleStrategyContext roleStrategyContext;

    @BeforeEach
    void setUp() {
        final List<RoleStrategy> strategies = List.of(this.clientStrategy, this.advisorStrategy, this.adminStrategy);
        this.roleStrategyContext = new RoleStrategyContext(strategies, this.logger);
    }

    @Test
    void shouldReturnClientRolesForClientRoleId() {
        // Arrange
        final Integer clientRoleId = BusinessConstants.CLIENT_ROLE_ID;
        final List<String> expectedRoles = List.of("CLIENT");

        when(this.clientStrategy.supports(clientRoleId)).thenReturn(true);
        lenient().when(this.advisorStrategy.supports(clientRoleId)).thenReturn(false);
        lenient().when(this.adminStrategy.supports(clientRoleId)).thenReturn(false);
        when(this.clientStrategy.getRoles()).thenReturn(expectedRoles);

        // Act
        final List<String> result = this.roleStrategyContext.getRolesForUser(clientRoleId);

        // Assert
        assertEquals(expectedRoles, result);
        verify(this.clientStrategy).supports(clientRoleId);
        verify(this.clientStrategy).getRoles();
    }

    @Test
    void shouldReturnAdvisorRolesForAdvisorRoleId() {
        // Arrange
        final Integer advisorRoleId = BusinessConstants.ADVISOR_ROLE_ID;
        final List<String> expectedRoles = List.of("ADVISOR");

        lenient().when(this.clientStrategy.supports(advisorRoleId)).thenReturn(false);
        when(this.advisorStrategy.supports(advisorRoleId)).thenReturn(true);
        lenient().when(this.adminStrategy.supports(advisorRoleId)).thenReturn(false);
        when(this.advisorStrategy.getRoles()).thenReturn(expectedRoles);

        // Act
        final List<String> result = this.roleStrategyContext.getRolesForUser(advisorRoleId);

        // Assert
        assertEquals(expectedRoles, result);
        verify(this.advisorStrategy).supports(advisorRoleId);
        verify(this.advisorStrategy).getRoles();
    }

    @Test
    void shouldReturnAdminRolesForAdminRoleId() {
        // Arrange
        final Integer adminRoleId = BusinessConstants.ADMIN_ROLE_ID;
        final List<String> expectedRoles = List.of("ADMIN");

        lenient().when(this.clientStrategy.supports(adminRoleId)).thenReturn(false);
        lenient().when(this.advisorStrategy.supports(adminRoleId)).thenReturn(false);
        when(this.adminStrategy.supports(adminRoleId)).thenReturn(true);
        when(this.adminStrategy.getRoles()).thenReturn(expectedRoles);

        // Act
        final List<String> result = this.roleStrategyContext.getRolesForUser(adminRoleId);

        // Assert
        assertEquals(expectedRoles, result);
        verify(this.adminStrategy).supports(adminRoleId);
        verify(this.adminStrategy).getRoles();
    }

    @Test
    void shouldReturnEmptyListForUnknownRoleId() {
        // Arrange
        final Integer unknownRoleId = 999;

        when(this.clientStrategy.supports(unknownRoleId)).thenReturn(false);
        when(this.advisorStrategy.supports(unknownRoleId)).thenReturn(false);
        when(this.adminStrategy.supports(unknownRoleId)).thenReturn(false);

        // Act
        final List<String> result = this.roleStrategyContext.getRolesForUser(unknownRoleId);

        // Assert
        assertTrue(result.isEmpty());
        verify(this.logger).warn("Unknown role ID: {}. Returning empty roles list.", unknownRoleId);
    }

    @Test
    void shouldReturnEmptyListForNullRoleId() {
        // Arrange
        when(this.clientStrategy.supports(null)).thenReturn(false);
        when(this.advisorStrategy.supports(null)).thenReturn(false);
        when(this.adminStrategy.supports(null)).thenReturn(false);

        // Act
        final List<String> result = this.roleStrategyContext.getRolesForUser(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(this.logger).warn("Unknown role ID: {}. Returning empty roles list.", (Object) null);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 4, 5, 10, 100, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void shouldReturnEmptyListForInvalidRoleIds(final Integer invalidRoleId) {
        // Arrange
        when(this.clientStrategy.supports(invalidRoleId)).thenReturn(false);
        when(this.advisorStrategy.supports(invalidRoleId)).thenReturn(false);
        when(this.adminStrategy.supports(invalidRoleId)).thenReturn(false);

        // Act
        final List<String> result = this.roleStrategyContext.getRolesForUser(invalidRoleId);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list for invalid role ID: " + invalidRoleId);
        verify(this.logger).warn("Unknown role ID: {}. Returning empty roles list.", invalidRoleId);
    }

    @Test
    void shouldUseFirstMatchingStrategy() {
        // Arrange - Both client and advisor strategies support the same role ID
        final Integer ambiguousRoleId = BusinessConstants.ADMIN_ROLE_ID;
        final List<String> clientRoles = List.of("CLIENT");

        when(this.clientStrategy.supports(ambiguousRoleId)).thenReturn(true);
        lenient().when(this.advisorStrategy.supports(ambiguousRoleId)).thenReturn(true);
        lenient().when(this.adminStrategy.supports(ambiguousRoleId)).thenReturn(false);
        when(this.clientStrategy.getRoles()).thenReturn(clientRoles);

        // Act
        final List<String> result = this.roleStrategyContext.getRolesForUser(ambiguousRoleId);

        // Assert
        assertEquals(clientRoles, result, "Should use first matching strategy");
        verify(this.clientStrategy).supports(ambiguousRoleId);
        verify(this.clientStrategy).getRoles();
    }

    @Test
    void shouldHandleEmptyStrategiesList() {
        // Arrange
        final RoleStrategyContext emptyContext = new RoleStrategyContext(List.of(), this.logger);
        final Integer roleId = BusinessConstants.ADMIN_ROLE_ID;

        // Act
        final List<String> result = emptyContext.getRolesForUser(roleId);

        // Assert
        assertTrue(result.isEmpty());
        verify(this.logger).warn("Unknown role ID: {}. Returning empty roles list.", roleId);
    }
}