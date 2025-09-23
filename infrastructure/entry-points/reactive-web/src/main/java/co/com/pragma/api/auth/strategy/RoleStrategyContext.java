package co.com.pragma.api.auth.strategy;

import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Contexto para el manejo de strategies de roles.
 * Coordina la selección de la strategy apropiada según el tipo de rol.
 */
@Component
public class RoleStrategyContext {

    private final List<RoleStrategy> strategies;
    private final LoggerPort logger;

    public RoleStrategyContext(List<RoleStrategy> strategies, LoggerPort logger) {
        this.strategies = strategies;
        this.logger = logger;
    }

    /**
     * Obtiene los roles correspondientes al roleId usando la strategy apropiada.
     *
     * @param roleId ID del rol del usuario
     * @return Lista de roles asignados al usuario
     */
    public List<String> getRolesForUser(Integer roleId) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(roleId))
                .findFirst()
                .map(RoleStrategy::getRoles)
                .orElseGet(() -> {
                    logger.warn("Unknown role ID: {}. Returning empty roles list.", roleId);
                    return List.of();
                });
    }
}