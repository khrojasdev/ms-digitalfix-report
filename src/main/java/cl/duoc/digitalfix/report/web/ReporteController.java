package cl.duoc.digitalfix.report.web;

import cl.duoc.digitalfix.report.domain.PeriodoReporte;
import cl.duoc.digitalfix.report.service.ContextoService;
import cl.duoc.digitalfix.report.service.ReporteService;
import cl.duoc.digitalfix.report.web.dto.IndicadoresDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReporteController {

    private final ReporteService reporteService;
    private final ContextoService contextoService;

    public ReporteController(ReporteService reporteService, ContextoService contextoService) {
        this.reporteService = reporteService;
        this.contextoService = contextoService;
    }

    @GetMapping("/indicators")
    public ResponseEntity<IndicadoresDto> obtenerIndicadores(
            @RequestParam(value = "periodo", defaultValue = "24h") String codigoPeriodo
    ) {
        // 1. EXIGENCIA DE NEGOCIO: Solo el rol ADMIN puede acceder a los reportes (HU-39.6)
        contextoService.exigirEmpresaAdmin();

        // 2. Valida el código temporal
        PeriodoReporte periodo = PeriodoReporte.desdeCodigo(codigoPeriodo);

        // 3. Calcula y devuelve los indicadores
        IndicadoresDto indicadores = reporteService.obtenerIndicadoresDesde(periodo.calcularFechaInicio());
        return ResponseEntity.ok(indicadores);
    }
}