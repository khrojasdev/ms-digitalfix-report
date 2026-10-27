package cl.duoc.digitalfix.report.service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Evento de orden de ms-digitalfix-workorders. Los nombres de este record no
 * coincidian con los que publica workorders (estadoNuevo, fecha) y llegaban en
 * null: el alias los conecta. Los campos que no se usan aqui se ignoran.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdenEventoDto(
        String eventKey,
        Long ordenId,
        String tipoEvento,     // CREADA, ASIGNADA, EN_DESPLAZAMIENTO, EN_EJECUCION, CERRADA, CANCELADA
        Long empresaId,
        Long servicioId,
        @JsonAlias("estadoNuevo") String estadoActual,
        @JsonAlias("fecha") LocalDateTime fechaEvento
) {
    /** El estado en que quedo la orden; si el evento no lo trae, sale del tipo. */
    public String estadoResultante() {
        if (estadoActual != null && !estadoActual.isBlank()) {
            return estadoActual;
        }
        return "INICIADA".equalsIgnoreCase(tipoEvento) ? "EN_EJECUCION" : tipoEvento;
    }
}
