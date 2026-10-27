package cl.duoc.digitalfix.report.mensajeria.ordenes;

import cl.duoc.digitalfix.report.mensajeria.ProcesadorDeMensajes;
import cl.duoc.digitalfix.report.service.ProyeccionDeOrdenes;
import cl.duoc.digitalfix.report.service.dto.OrdenEventoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio ordenes: alimenta los indicadores con cada evento de orden. La
 * decision de ACK, reintento o DLQ la toma {@link ProcesadorDeMensajes}.
 */
@Component
public class OrdenEventoConsumer {

    private final ProyeccionDeOrdenes proyeccion;
    private final ObjectMapper objectMapper;
    private final ProcesadorDeMensajes procesador;
    private final String cola;

    public OrdenEventoConsumer(ProyeccionDeOrdenes proyeccion, ObjectMapper objectMapper,
                               ProcesadorDeMensajes procesador, @Value("${amqp.queue.reportes}") String cola) {
        this.proyeccion = proyeccion;
        this.objectMapper = objectMapper;
        this.procesador = procesador;
        this.cola = cola;
    }

    @RabbitListener(queues = "${amqp.queue.reportes}")
    public void consumirEvento(String mensajeJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesador.procesar(cola, channel, tag,
                () -> proyeccion.aplicar(objectMapper.readValue(mensajeJson, OrdenEventoDto.class)));
    }
}
