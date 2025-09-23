package co.com.pragma.api.auth.strategy;

import co.com.pragma.model.constants.BusinessConstants;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleStrategyContextTest {

    @Mock private RoleStrategy clientStrategy;
    @Mock private RoleStrategy advisorStrategy;
    @Mock private RoleStrategy adminStrategy;
    @Mock private LoggerPort logger;

    private RoleStrategyContext roleStrategyContext;

    @BeforeEach
    void setUp() {
        List<RoleStrategy> strategies = List.of(clientStrategy, advisorStrategy, adminStrategy);
        roleStrategyContext = new RoleStrategyContext(strategies, logger);
    }

    @Test
    void shouldReturnClientRoles() {
        Integer clientRoleId = BusinessConstants.CLIENT_ROLE_ID;
        List<String> expectedRoles = List.of("CLIENT");

        when(clientStrategy.supports(clientRoleId)).thenReturn(true);
        when(clientStrategy.getRoles()).thenReturn(expectedRoles);

        List<String> result = roleStrategyContext.getRolesForUser(clientRoleId);

        assertEquals(expectedRoles, result);
        verify(clientStrategy).supports(clientRoleId);
        verify(clientStrategy).getRoles();
    }

    @Test
    void shouldReturnAdvisorRoles() {
        Integer advisorRoleId = BusinessConstants.ADVISOR_ROLE_ID;
        List<String> expectedRoles = List.of("ADVISOR");

        when(advisorStrategy.supports(advisorRoleId)).thenReturn(true);
        when(advisorStrategy.getRoles()).thenReturn(expectedRoles);

        List<String> result = roleStrategyContext.getRolesForUser(advisorRoleId);

        assertEquals(expectedRoles, result);
        verify(advisorStrategy).supports(advisorRoleId);
        verify(advisorStrategy).getRoles();
    }

    @Test
    void shouldReturnAdminRoles() {
        Integer adminRoleId = BusinessConstants.ADMIN_ROLE_ID;
        List<String> expectedRoles = List.of("ADMIN");

        when(adminStrategy.supports(adminRoleId)).thenReturn(true);
        when(adminStrategy.getRoles()).thenReturn(expectedRoles);

        List<String> result = roleStrategyContext.getRolesForUser(adminRoleId);

        assertEquals(expectedRoles, result);
        verify(adminStrategy).supports(adminRoleId);
        verify(adminStrategy).getRoles();
    }

    @Test
    void shouldReturnEmptyForNullRoleId() {
        when(clientStrategy.supports(null)).thenReturn(false);
        when(advisorStrategy.supports(null)).thenReturn(false);
        when(adminStrategy.supports(null)).thenReturn(false);

        List<String> result = roleStrategyContext.getRolesForUser(null);

        assertTrue(result.isEmpty());
        verify(logger).warn("Unknown role ID: {}. Returning empty roles list.", (Object) null);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 4, 5, 999})
    void shouldReturnEmptyForInvalidRoleIds(Integer invalidRoleId) {
        when(clientStrategy.supports(invalidRoleId)).thenReturn(false);
        when(advisorStrategy.supports(invalidRoleId)).thenReturn(false);
        when(adminStrategy.supports(invalidRoleId)).thenReturn(false);

        List<String> result = roleStrategyContext.getRolesForUser(invalidRoleId);

        assertTrue(result.isEmpty());
        verify(logger).warn("Unknown role ID: {}. Returning empty roles list.", invalidRoleId);
    }

    @Test
    void shouldUseFirstMatchingStrategy() {
        Integer roleId = 1;
        List<String> firstStrategyRoles = List.of("FIRST");

        when(clientStrategy.supports(roleId)).thenReturn(true);
        when(clientStrategy.getRoles()).thenReturn(firstStrategyRoles);

        List<String> result = roleStrategyContext.getRolesForUser(roleId);

        assertEquals(firstStrategyRoles, result);
        verify(clientStrategy).supports(roleId);
        verify(clientStrategy).getRoles();
    }

    @Test
    void shouldHandleEmptyStrategiesList() {
        RoleStrategyContext emptyContext = new RoleStrategyContext(List.of(), logger);

        List<String> result = emptyContext.getRolesForUser(1);

        assertTrue(result.isEmpty());
        verify(logger).warn("Unknown role ID: {}. Returning empty roles list.", 1);
    }
}