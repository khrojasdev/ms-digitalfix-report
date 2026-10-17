package cl.duoc.digitalfix.report.web;

import cl.duoc.digitalfix.report.domain.PeriodoReporte;
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

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/indicators")
    public ResponseEntity<IndicadoresDto> obtenerIndicadores(
            @RequestParam(value = "periodo", defaultValue = "24h") String codigoPeriodo
    ) {
        // Valida el código y calcula la fecha límite automáticamente
        PeriodoReporte periodo = PeriodoReporte.desdeCodigo(codigoPeriodo);

        IndicadoresDto indicadores = reporteService.obtenerIndicadoresDesde(periodo.calcularFechaInicio());
        return ResponseEntity.ok(indicadores);
    }
}