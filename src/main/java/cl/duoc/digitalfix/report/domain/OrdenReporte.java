package cl.duoc.digitalfix.report.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_reporte")
public class OrdenReporte {

    @Id
    private Long ordenId;
    private String estado;
    private Long servicioId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
    private Long tiempoResolucionMinutos;

    // Constructores, Getters y Setters
    public OrdenReporte() {}

    public OrdenReporte(Long ordenId, String estado, Long servicioId, LocalDateTime fechaCreacion) {
        this.ordenId = ordenId;
        this.estado = estado;
        this.servicioId = servicioId;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getOrdenId() { return ordenId; }
    public void setOrdenId(Long ordenId) { this.ordenId = ordenId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getServicioId() { return servicioId; }
    public void setServicioId(Long servicioId) { this.servicioId = servicioId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public Long getTiempoResolucionMinutos() { return tiempoResolucionMinutos; }
    public void setTiempoResolucionMinutos(Long tiempoResolucionMinutos) { this.tiempoResolucionMinutos = tiempoResolucionMinutos; }
}