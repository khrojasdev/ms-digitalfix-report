package cl.duoc.digitalfix.report.web.dto;

import java.util.Map;

public record IndicadoresDto(
        long ordenesTotales,
        Double tiempoMedioResolucionMinutos,
        Map<String, Long> conteoPorEstado
) {}