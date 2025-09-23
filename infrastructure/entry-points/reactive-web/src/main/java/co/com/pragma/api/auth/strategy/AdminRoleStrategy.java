package co.com.pragma.api.auth.strategy;

import co.com.pragma.model.constants.BusinessConstants;
import co.com.pragma.security.model.RoleConstants;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy para manejo de roles de administrador.
 */
@Component
public class AdminRoleStrategy implements RoleStrategy {

    @Override
    public boolean supports(Integer roleId) {
        return BusinessConstants.ADMIN_ROLE_ID.equals(roleId);
    }

    @Override
    public List<String> getRoles() {
        return List.of(RoleConstants.ADMIN);
    }
}