package cl.duoc.digitalfix.report.service;

import cl.duoc.digitalfix.report.config.ContextoEmpresa;
import cl.duoc.digitalfix.report.config.ContextoEmpresaHolder;
import cl.duoc.digitalfix.report.error.AccesoDenegado;
import cl.duoc.digitalfix.report.error.SolicitudInvalida;
import org.springframework.stereotype.Service;

@Service
public class ContextoService {

    public ContextoEmpresa obtenerContextoObligatorio() {
        return ContextoEmpresaHolder.obtener()
                .orElseThrow(() -> new SolicitudInvalida("Falta la cabecera de contexto de empresa (X-Company-Id)"));
    }

    public Long exigirEmpresaAdmin() {
        ContextoEmpresa contexto = obtenerContextoObligatorio();
        if (!contexto.esAdmin()) {
            throw new AccesoDenegado("Acceso restringido exclusivamente al rol ADMIN");
        }
        return contexto.companyId();
    }
}