package co.com.pragma.api.auth.strategy;

import co.com.pragma.model.constants.BusinessConstants;
import co.com.pragma.security.model.RoleConstants;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy para manejo de roles de cliente.
 */
@Component
public class ClientRoleStrategy implements RoleStrategy {

    @Override
    public boolean supports(final Integer roleId) {
        return BusinessConstants.CLIENT_ROLE_ID.equals(roleId);
    }

    @Override
    public List<String> getRoles() {
        return List.of(RoleConstants.CLIENT);
    }
}