package co.com.pragma.api.auth.strategy;

import co.com.pragma.model.constants.BusinessConstants;
import co.com.pragma.security.model.RoleConstants;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy para manejo de roles de asesor.
 */
@Component
public class AdvisorRoleStrategy implements RoleStrategy {

    @Override
    public boolean supports(Integer roleId) {
        return BusinessConstants.ADVISOR_ROLE_ID.equals(roleId);
    }

    @Override
    public List<String> getRoles() {
        return List.of(RoleConstants.ADVISOR);
    }
}