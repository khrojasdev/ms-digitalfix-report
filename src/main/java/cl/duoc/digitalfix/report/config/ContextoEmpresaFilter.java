package cl.duoc.digitalfix.report.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ContextoEmpresaFilter extends OncePerRequestFilter {

    public static final String HEADER_OID = "X-User-Oid";
    public static final String HEADER_EMPRESA = "X-Company-Id";
    public static final String HEADER_ROLES = "X-Roles";
    public static final String HEADER_CORRELACION = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlacion = request.getHeader(HEADER_CORRELACION);
        if (correlacion != null && !correlacion.isBlank()) {
            MDC.put("correlacion", correlacion);
        }

        try {
            String oid = request.getHeader(HEADER_OID);
            String empresaStr = request.getHeader(HEADER_EMPRESA);
            String rolesStr = request.getHeader(HEADER_ROLES);

            if (empresaStr != null && !empresaStr.isBlank()) {
                Long companyId = Long.parseLong(empresaStr.trim());
                Set<String> roles = parsearRoles(rolesStr);
                ContextoEmpresaHolder.establecer(new ContextoEmpresa(oid, companyId, roles, correlacion));
            }

            filterChain.doFilter(request, response);
        } finally {
            ContextoEmpresaHolder.limpiar();
            MDC.remove("correlacion");
        }
    }

    private Set<String> parsearRoles(String rolesStr) {
        if (rolesStr == null || rolesStr.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(rolesStr.split(","))
                .map(String::trim)
                .filter(r -> !r.isEmpty())
                .collect(Collectors.toSet());
    }
}