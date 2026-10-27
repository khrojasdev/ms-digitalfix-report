package cl.duoc.digitalfix.report.service;

import cl.duoc.digitalfix.report.repository.OrdenReporteRepository;
import cl.duoc.digitalfix.report.web.dto.IndicadoresDto;
import cl.duoc.digitalfix.report.web.dto.OrdenesPorHoraDto;
import cl.duoc.digitalfix.report.web.dto.ServicioRankingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final OrdenReporteRepository repository;

    public ReporteService(OrdenReporteRepository repository) {
        this.repository = repository;
    }

    public IndicadoresDto obtenerIndicadoresDesde(Long empresaId, LocalDateTime fechaDesde) {
        long totalOrdenes = repository.countByEmpresaIdAndFechaCreacionAfter(empresaId, fechaDesde);
        Double tiempoMedio = repository.getTiempoMedioResolucionMinutosDesde(empresaId, fechaDesde);

        Map<String, Long> conteoPorEstado = repository.countByEstadoDesde(empresaId, fechaDesde).stream()
                .collect(Collectors.toMap(fila -> (String) fila[0], fila -> ((Number) fila[1]).longValue()));

        return new IndicadoresDto(totalOrdenes, tiempoMedio, conteoPorEstado);
    }

    public List<ServicioRankingDto> obtenerRankingServiciosDesde(Long empresaId, LocalDateTime fechaDesde) {
        return repository.findTopServiciosDesde(empresaId, fechaDesde).stream()
                .map(fila -> new ServicioRankingDto(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue()))
                .collect(Collectors.toList());
    }

    /**
     * KPI del caso "ordenes por hora": cuantas ordenes se crearon en cada hora
     * del dia (0 a 23) dentro del periodo. Siempre trae las 24 horas, con 0
     * donde no hubo ordenes, para que el grafico no tenga huecos.
     */
    public List<OrdenesPorHoraDto> obtenerOrdenesPorHoraDesde(Long empresaId, LocalDateTime fechaDesde) {
        long[] conteo = new long[24];
        for (LocalDateTime fecha : repository.fechasDeCreacionDesde(empresaId, fechaDesde)) {
            if (fecha != null) {
                conteo[fecha.getHour()]++;
            }
        }
        List<OrdenesPorHoraDto> resultado = new ArrayList<>(24);
        for (int hora = 0; hora < 24; hora++) {
            resultado.add(new OrdenesPorHoraDto(hora, conteo[hora]));
        }
        return resultado;
    }
}
