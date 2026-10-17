package cl.duoc.digitalfix.report.service;

import cl.duoc.digitalfix.report.domain.OrdenReporte;
import cl.duoc.digitalfix.report.repository.OrdenReporteRepository;
import cl.duoc.digitalfix.report.service.dto.OrdenEventoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @KafkaListener(topics = "ordenes-negocio-topic", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumirEvento(String mensajeJson) {
        try {
            OrdenEventoDto evento = objectMapper.readValue(mensajeJson, OrdenEventoDto.class);
            log.info("Procesando evento de orden {}: {}", evento.ordenId(), evento.tipoEvento());

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
        } catch (JsonProcessingException e) {
            log.error("Error al deserializar evento de Kafka: {}", mensajeJson, e);
            // La HU-42.4 pide apartar los eventos fallidos, lo implementaremos en esa issue específica.
        }
    }
}