package cl.duoc.digitalfix.report.service;

import cl.duoc.digitalfix.report.repository.OrdenReporteRepository;
import cl.duoc.digitalfix.report.web.dto.IndicadoresDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final OrdenReporteRepository repository;

    public ReporteService(OrdenReporteRepository repository) {
        this.repository = repository;
    }

    public IndicadoresDto obtenerIndicadoresDesde(LocalDateTime fechaDesde) {
        long totalOrdenes = repository.countByFechaCreacionAfter(fechaDesde);
        Double tiempoMedio = repository.getTiempoMedioResolucionMinutosDesde(fechaDesde);

        // Transformar la lista de Object[] que devuelve Spring Data a un Map
        List<Object[]> resultadosEstado = repository.countByEstadoDesde(fechaDesde);
        Map<String, Long> conteoPorEstado = resultadosEstado.stream()
                .collect(Collectors.toMap(
                        fila -> (String) fila[0],
                        fila -> (Long) fila[1]
                ));

        return new IndicadoresDto(totalOrdenes, tiempoMedio, conteoPorEstado);
    }
}