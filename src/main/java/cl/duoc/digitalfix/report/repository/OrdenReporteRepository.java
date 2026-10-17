package cl.duoc.digitalfix.report.repository;

import cl.duoc.digitalfix.report.domain.OrdenReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdenReporteRepository extends JpaRepository<OrdenReporte, Long> {

    // HU-39.4: Conteo total de órdenes en un periodo
    long countByFechaCreacionAfter(LocalDateTime fecha);

    // HU-39.4: Tiempo medio de resolución en minutos para órdenes cerradas en el periodo
    @Query("SELECT AVG(o.tiempoResolucionMinutos) FROM OrdenReporte o WHERE o.fechaCierre >= :fecha")
    Double getTiempoMedioResolucionMinutosDesde(@Param("fecha") LocalDateTime fecha);

    // HU-39.4: Conteo de órdenes agrupadas por estado (ej. "ASIGNADA" -> 5, "CERRADA" -> 10)
    @Query("SELECT o.estado, COUNT(o) FROM OrdenReporte o WHERE o.fechaCreacion >= :fecha GROUP BY o.estado")
    List<Object[]> countByEstadoDesde(@Param("fecha") LocalDateTime fecha);

    // HU-40.1 y HU-40.2: Ranking de servicios más solicitados
    // El orden es por cantidad descendente, y en caso de empate, por ID de servicio ascendente para ser estable.
    @Query("SELECT o.servicioId, COUNT(o) AS cantidad " +
            "FROM OrdenReporte o " +
            "WHERE o.fechaCreacion >= :fecha " +
            "GROUP BY o.servicioId " +
            "ORDER BY cantidad DESC, o.servicioId ASC")
    List<Object[]> findTopServiciosDesde(@Param("fecha") LocalDateTime fecha);
}