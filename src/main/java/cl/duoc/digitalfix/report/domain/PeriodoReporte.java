package cl.duoc.digitalfix.report.domain;

import cl.duoc.digitalfix.report.error.SolicitudInvalida;

import java.time.LocalDateTime;

public enum PeriodoReporte {
    H24("24h", 24),
    D7("7d", 24 * 7),
    D30("30d", 24 * 30);

    private final String codigo;
    private final int horasAtras;

    PeriodoReporte(String codigo, int horasAtras) {
        this.codigo = codigo;
        this.horasAtras = horasAtras;
    }

    public LocalDateTime calcularFechaInicio() {
        return LocalDateTime.now().minusHours(horasAtras);
    }

    public static PeriodoReporte desdeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return H24; // Valor por defecto
        }
        for (PeriodoReporte periodo : values()) {
            if (periodo.codigo.equalsIgnoreCase(codigo.trim())) {
                return periodo;
            }
        }
        throw new SolicitudInvalida("Periodo no soportado: '" + codigo + "'. Los valores válidos son: 24h, 7d, 30d.");
    }
}