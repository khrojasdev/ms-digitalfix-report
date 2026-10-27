package cl.duoc.digitalfix.report.web;

import cl.duoc.digitalfix.report.mensajeria.ordenes.OrdenEventoConsumer;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * De punta a punta contra H2: los eventos tal como los publica workorders
 * llegan al consumidor y los reportes los cuentan, por empresa.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReportesTest {

    private static final AtomicLong SECUENCIA = new AtomicLong(1000);

    @Autowired private MockMvc mvc;
    @Autowired private OrdenEventoConsumer consumidor;

    private final Channel canal = mock(Channel.class);

    /** Mismo formato que publica ms-digitalfix-workorders. */
    private void evento(long orden, long empresa, String tipo, String estado, long servicio, LocalDateTime fecha) throws Exception {
        String json = "{\"eventKey\":\"k-" + orden + "-" + tipo + "\",\"tipoEvento\":\"" + tipo + "\",\"ordenId\":" + orden
                + ",\"autorId\":\"oid\",\"empresaId\":" + empresa + ",\"fecha\":\"" + fecha.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + "\",\"correlacionId\":null,\"estadoAnterior\":null,\"estadoNuevo\":\"" + estado + "\",\"servicioId\":" + servicio
                + ",\"tecnicoNombre\":null,\"nota\":null}";
        long tag = SECUENCIA.incrementAndGet();
        consumidor.consumirEvento(json, canal, tag);
        verify(canal).basicAck(tag, false);
    }

    private ResultActions comoAdmin(String empresa, String ruta) throws Exception {
        return mvc.perform(get(ruta).header("X-User-Oid", "oid-admin").header("X-Company-Id", empresa).header("X-Roles", "ADMIN"));
    }

    @Test
    void cuentaLasOrdenesDeLaEmpresaConSuTiempoDeResolucion() throws Exception {
        LocalDateTime hace2h = LocalDateTime.now().minusHours(2).withMinute(0).withSecond(0).withNano(0);
        long a = SECUENCIA.incrementAndGet();
        long b = SECUENCIA.incrementAndGet();
        long deOtra = SECUENCIA.incrementAndGet();
        evento(a, 51, "CREADA", "CREADA", 7, hace2h);
        evento(a, 51, "ASIGNADA", "ASIGNADA", 7, hace2h.plusMinutes(10));
        evento(a, 51, "CERRADA", "CERRADA", 7, hace2h.plusMinutes(90));
        evento(b, 51, "CREADA", "CREADA", 8, hace2h.plusMinutes(5));
        evento(deOtra, 52, "CREADA", "CREADA", 7, hace2h);

        comoAdmin("51", "/api/reports/indicators?periodo=24h")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordenesTotales").value(2))
                .andExpect(jsonPath("$.tiempoMedioResolucionMinutos").value(90.0))
                .andExpect(jsonPath("$.conteoPorEstado.CERRADA").value(1))
                .andExpect(jsonPath("$.conteoPorEstado.CREADA").value(1));

        comoAdmin("51", "/api/reports/top-services?periodo=24h")
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void ordenesPorHoraTraeLas24HorasConSusConteos() throws Exception {
        LocalDateTime base = LocalDateTime.now().minusHours(3).withMinute(15);
        evento(SECUENCIA.incrementAndGet(), 61, "CREADA", "CREADA", 1, base);
        evento(SECUENCIA.incrementAndGet(), 61, "CREADA", "CREADA", 1, base.plusMinutes(20));
        evento(SECUENCIA.incrementAndGet(), 61, "CREADA", "CREADA", 1, base.plusHours(1));

        comoAdmin("61", "/api/reports/orders-by-hour?periodo=24h")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(24))
                .andExpect(jsonPath("$[" + base.getHour() + "].cantidad").value(base.plusMinutes(20).getHour() == base.getHour() ? 2 : 1))
                .andExpect(jsonPath("$[" + base.plusHours(1).getHour() + "].hora").value(base.plusHours(1).getHour()));
    }

    @Test
    void soloElAdminVeReportesYElPeriodoSeValida() throws Exception {
        mvc.perform(get("/api/reports/indicators").header("X-User-Oid", "o").header("X-Company-Id", "51")
                .header("X-Roles", "CLIENTE")).andExpect(status().isForbidden());
        comoAdmin("51", "/api/reports/orders-by-hour?periodo=1año").andExpect(status().isBadRequest());
    }
}
