package co.com.pragma.api.auth.strategy;

import java.util.List;

/**
 * Strategy interface para manejo de roles por tipo de usuario.
 * Permite extensibilidad y mantenimiento más fácil de los roles.
 */
public interface RoleStrategy {

    /**
     * Verifica si esta strategy puede manejar el roleId dado.
     *
     * @param roleId ID del rol a verificar
     * @return true si puede manejar este rol, false en caso contrario
     */
    boolean supports(Integer roleId);

    /**
     * Obtiene la lista de roles para el usuario.
     *
     * @return Lista de roles asignados
     */
    List<String> getRoles();
}