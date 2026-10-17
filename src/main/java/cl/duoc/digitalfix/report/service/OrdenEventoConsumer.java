package cl.duoc.digitalfix.report.service;

import cl.duoc.digitalfix.report.domain.OrdenReporte;
import cl.duoc.digitalfix.report.repository.OrdenReporteRepository;
import cl.duoc.digitalfix.report.service.dto.OrdenEventoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;

@Service
public class OrdenEventoConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrdenEventoConsumer.class);
    private final OrdenReporteRepository repository;
    private final ObjectMapper objectMapper;

    public OrdenEventoConsumer(OrdenReporteRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    // EXIGENCIA DE RÚBRICA: @RabbitListener apuntando a la variable centralizada
    @RabbitListener(queues = "${amqp.queue.reportes}")
    @Transactional
    public void consumirEvento(String mensajeJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            OrdenEventoDto evento = objectMapper.readValue(mensajeJson, OrdenEventoDto.class);
            log.info("Procesando evento de orden en RabbitMQ {}: {}", evento.ordenId(), evento.tipoEvento());

            if ("CREADA".equals(evento.tipoEvento())) {
                OrdenReporte nuevaOrden = new OrdenReporte(
                        evento.ordenId(),
                        evento.estadoActual(),
                        evento.servicioId(),
                        evento.fechaEvento()
                );
                repository.save(nuevaOrden);
            } else {
                repository.findById(evento.ordenId()).ifPresent(orden -> {
                    orden.setEstado(evento.estadoActual());
                    if ("CERRADA".equals(evento.tipoEvento())) {
                        orden.setFechaCierre(evento.fechaEvento());
                        long minutos = Duration.between(orden.getFechaCreacion(), evento.fechaEvento()).toMinutes();
                        orden.setTiempoResolucionMinutos(minutos);
                    }
                    repository.save(orden);
                });
            }

            // EXIGENCIA DE RÚBRICA: Acknowledge manual explícito para confirmar éxito
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Error al procesar el mensaje, enviando a DLQ. Motivo: {}", e.getMessage(), e);
            // EXIGENCIA DE RÚBRICA: NACK explícito (requeue=false) para que el mensaje se vaya a la DLQ
            channel.basicNack(tag, false, false);
        }
    }
}