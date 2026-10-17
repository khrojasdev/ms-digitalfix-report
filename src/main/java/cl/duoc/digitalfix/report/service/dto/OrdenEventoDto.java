package cl.duoc.digitalfix.report.service.dto;

import java.time.LocalDateTime;

public record OrdenEventoDto(
        Long ordenId,
        String tipoEvento, // CREADA, ASIGNADA, INICIADA, CERRADA, CANCELADA
        Long servicioId,
        String estadoActual,
        LocalDateTime fechaEvento
) {}