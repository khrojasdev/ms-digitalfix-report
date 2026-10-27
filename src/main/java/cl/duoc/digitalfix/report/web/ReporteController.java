package cl.duoc.digitalfix.report.web;

import cl.duoc.digitalfix.report.domain.PeriodoReporte;
import cl.duoc.digitalfix.report.service.ContextoService;
import cl.duoc.digitalfix.report.service.ReporteService;
import cl.duoc.digitalfix.report.web.dto.IndicadoresDto;
import cl.duoc.digitalfix.report.web.dto.OrdenesPorHoraDto;
import cl.duoc.digitalfix.report.web.dto.ServicioRankingDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

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
        // 1. Solo el rol ADMIN puede acceder a los reportes (HU-39.6), y siempre de su empresa
        Long empresaId = contextoService.exigirEmpresaAdmin();

        // 2. Valida el código temporal
        PeriodoReporte periodo = PeriodoReporte.desdeCodigo(codigoPeriodo);

        // 3. Calcula y devuelve los indicadores
        IndicadoresDto indicadores = reporteService.obtenerIndicadoresDesde(empresaId, periodo.calcularFechaInicio());
        return ResponseEntity.ok(indicadores);
    }

    @GetMapping("/top-services")
    public ResponseEntity<List<ServicioRankingDto>> obtenerRankingServicios(
            @RequestParam(value = "periodo", defaultValue = "24h") String codigoPeriodo
    ) {
        // 1. Validar que quien llama sea ADMIN (y de que empresa es)
        Long empresaId = contextoService.exigirEmpresaAdmin();

        // 2. Validar el parámetro de tiempo (ej. "7d")
        PeriodoReporte periodo = PeriodoReporte.desdeCodigo(codigoPeriodo);

        // 3. Consultar la tabla de lectura y devolver la lista (o lista vacía)
        List<ServicioRankingDto> ranking = reporteService.obtenerRankingServiciosDesde(empresaId, periodo.calcularFechaInicio());

        return ResponseEntity.ok(ranking);
    }

    /**
     * KPI del caso "ordenes por hora": GET /api/reports/orders-by-hour?periodo=7d
     * devuelve las 24 horas del dia con cuantas ordenes se crearon en cada una.
     */
    @GetMapping("/orders-by-hour")
    public ResponseEntity<List<OrdenesPorHoraDto>> obtenerOrdenesPorHora(
            @RequestParam(value = "periodo", defaultValue = "24h") String codigoPeriodo
    ) {
        Long empresaId = contextoService.exigirEmpresaAdmin();
        PeriodoReporte periodo = PeriodoReporte.desdeCodigo(codigoPeriodo);
        return ResponseEntity.ok(reporteService.obtenerOrdenesPorHoraDesde(empresaId, periodo.calcularFechaInicio()));
    }
}