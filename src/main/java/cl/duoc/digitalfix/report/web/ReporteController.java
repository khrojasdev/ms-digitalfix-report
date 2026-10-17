package cl.duoc.digitalfix.report.web;

import cl.duoc.digitalfix.report.service.ReporteService;
import cl.duoc.digitalfix.report.web.dto.IndicadoresDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/indicators")
    public ResponseEntity<IndicadoresDto> obtenerIndicadores() {
        // Calculamos la fecha límite (24 horas hacia atrás)
        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);

        IndicadoresDto indicadores = reporteService.obtenerIndicadoresDesde(hace24Horas);
        return ResponseEntity.ok(indicadores);
    }
}