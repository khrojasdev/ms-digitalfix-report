package cl.duoc.digitalfix.report.config;

import java.util.Set;

/**
 * Contexto del usuario y empresa propagado por el BFF mediante cabeceras HTTP.
 */
public record ContextoEmpresa(
        String userOid,
        Long companyId,
        Set<String> roles,
        String correlacion
) {
    public boolean esAdmin() {
        return roles != null && roles.stream()
                .anyMatch(rol -> rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("ROLE_ADMIN"));
    }
}