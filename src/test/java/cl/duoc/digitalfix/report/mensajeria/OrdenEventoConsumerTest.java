package cl.duoc.digitalfix.report.mensajeria;

import cl.duoc.digitalfix.report.mensajeria.ordenes.OrdenEventoConsumer;
import cl.duoc.digitalfix.report.repository.OrdenReporteRepository;
import cl.duoc.digitalfix.report.service.ProyeccionDeOrdenes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** ACK, DLQ y reintento del consumidor de reportes, sin base ni broker. */
class OrdenEventoConsumerTest {

    private final OrdenReporteRepository repositorio = mock(OrdenReporteRepository.class);
    private final Channel canal = mock(Channel.class);
    private final OrdenEventoConsumer consumidor = new OrdenEventoConsumer(new ProyeccionDeOrdenes(repositorio),
            new ObjectMapper().findAndRegisterModules(), new ProcesadorDeMensajes(3, 1), "ordenes.reportes.queue");

    @Test
    void eventoSinEmpresaVaALaDlqSinTocarLaBase() throws Exception {
        consumidor.consumirEvento("{\"ordenId\":1,\"tipoEvento\":\"CREADA\",\"servicioId\":2}", canal, 1L);
        verify(repositorio, never()).save(any());
        verify(canal).basicNack(1L, false, false);
    }

    @Test
    void siLaBaseNoRespondeSeReintentaYLuegoVaALaDlq() throws Exception {
        when(repositorio.findById(any())).thenThrow(new DataAccessResourceFailureException("base caida"));
        consumidor.consumirEvento("{\"ordenId\":1,\"tipoEvento\":\"CREADA\",\"empresaId\":1,\"servicioId\":2}", canal, 2L);
        verify(repositorio, times(3)).findById(1L);
        verify(canal).basicNack(2L, false, false);
    }

    @Test
    void unaOrdenQueNoEstabaSeAgregaAunqueLlegueSinSuCreada() throws Exception {
        when(repositorio.findById(5L)).thenReturn(Optional.empty());
        when(repositorio.save(any())).thenAnswer(i -> i.getArgument(0));
        consumidor.consumirEvento("{\"ordenId\":5,\"tipoEvento\":\"ASIGNADA\",\"estadoNuevo\":\"ASIGNADA\",\"empresaId\":1,"
                + "\"servicioId\":2,\"fecha\":\"2026-10-27T10:00:00\"}", canal, 3L);
        verify(repositorio).save(any());
        verify(canal).basicAck(3L, false);
    }
}
