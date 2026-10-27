package cl.duoc.digitalfix.report.service;

import cl.duoc.digitalfix.report.domain.OrdenReporte;
import cl.duoc.digitalfix.report.mensajeria.MensajeInvalido;
import cl.duoc.digitalfix.report.repository.OrdenReporteRepository;
import cl.duoc.digitalfix.report.service.dto.OrdenEventoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Mantiene el modelo de lectura de reportes (ordenes_reporte) a partir de los
 * eventos de orden. No conoce RabbitMQ: el consumidor le entrega el evento ya
 * leido.
 *
 * - CREADA: agrega la orden (o la reemplaza si el evento llega repetido).
 * - Otro tipo: actualiza el estado; en CERRADA calcula el tiempo de
 *   resolucion. Si la orden no esta (su CREADA se perdio), la agrega igual.
 */
@Service
public class ProyeccionDeOrdenes {

    private static final Logger log = LoggerFactory.getLogger(ProyeccionDeOrdenes.class);

    private final OrdenReporteRepository repository;

    public ProyeccionDeOrdenes(OrdenReporteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrdenReporte aplicar(OrdenEventoDto evento) {
        if (evento.ordenId() == null || evento.tipoEvento() == null || evento.empresaId() == null) {
            throw new MensajeInvalido("evento de orden incompleto: faltan orden, tipo o empresa");
        }
        LocalDateTime fecha = evento.fechaEvento() == null ? LocalDateTime.now() : evento.fechaEvento();
        String estado = evento.estadoResultante();

        OrdenReporte orden = repository.findById(evento.ordenId()).orElse(null);
        if (orden == null) {
            if (evento.servicioId() == null) {
                throw new MensajeInvalido("la orden " + evento.ordenId() + " no esta en el reporte y el evento no trae servicio");
            }
            orden = new OrdenReporte(evento.ordenId(), evento.empresaId(), estado, evento.servicioId(), fecha);
        } else {
            orden.setEstado(estado);
            if (orden.getEmpresaId() == null) {
                orden.setEmpresaId(evento.empresaId());
            }
        }

        if ("CERRADA".equalsIgnoreCase(evento.tipoEvento())) {
            orden.setFechaCierre(fecha);
            orden.setTiempoResolucionMinutos(Duration.between(orden.getFechaCreacion(), fecha).toMinutes());
        }
        OrdenReporte guardada = repository.save(orden);
        log.info("Reporte: orden {} -> {} (empresa {})", evento.ordenId(), estado, evento.empresaId());
        return guardada;
    }
}
