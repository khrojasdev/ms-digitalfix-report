package cl.duoc.digitalfix.report.repository;

import cl.duoc.digitalfix.report.domain.OrdenReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/** Todas las consultas filtran por empresa: un reporte nunca mezcla empresas. */
public interface OrdenReporteRepository extends JpaRepository<OrdenReporte, Long> {

    // HU-39.4: Conteo total de ordenes en un periodo
    long countByEmpresaIdAndFechaCreacionAfter(Long empresaId, LocalDateTime fecha);

    // HU-39.4: Tiempo medio de resolucion en minutos para ordenes cerradas en el periodo
    @Query("SELECT AVG(o.tiempoResolucionMinutos) FROM OrdenReporte o "
            + "WHERE o.empresaId = :empresa AND o.fechaCierre >= :fecha")
    Double getTiempoMedioResolucionMinutosDesde(@Param("empresa") Long empresaId, @Param("fecha") LocalDateTime fecha);

    // HU-39.4: Conteo de ordenes agrupadas por estado
    @Query("SELECT o.estado, COUNT(o) FROM OrdenReporte o "
            + "WHERE o.empresaId = :empresa AND o.fechaCreacion >= :fecha GROUP BY o.estado")
    List<Object[]> countByEstadoDesde(@Param("empresa") Long empresaId, @Param("fecha") LocalDateTime fecha);

    // HU-40.1 y HU-40.2: Ranking de servicios mas solicitados (empate: id ascendente)
    @Query("SELECT o.servicioId, COUNT(o) AS cantidad FROM OrdenReporte o "
            + "WHERE o.empresaId = :empresa AND o.fechaCreacion >= :fecha "
            + "GROUP BY o.servicioId ORDER BY cantidad DESC, o.servicioId ASC")
    List<Object[]> findTopServiciosDesde(@Param("empresa") Long empresaId, @Param("fecha") LocalDateTime fecha);

    // KPI del caso: ordenes por hora. Se agrupa en Java para no depender de
    // funciones de fecha propias de cada base.
    @Query("SELECT o.fechaCreacion FROM OrdenReporte o WHERE o.empresaId = :empresa AND o.fechaCreacion >= :fecha")
    List<LocalDateTime> fechasDeCreacionDesde(@Param("empresa") Long empresaId, @Param("fecha") LocalDateTime fecha);
}
