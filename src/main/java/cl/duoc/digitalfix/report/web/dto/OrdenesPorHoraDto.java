package cl.duoc.digitalfix.report.web.dto;

/** Ordenes creadas en una hora del dia (0 a 23) dentro del periodo. */
public record OrdenesPorHoraDto(int hora, long cantidad) {}
