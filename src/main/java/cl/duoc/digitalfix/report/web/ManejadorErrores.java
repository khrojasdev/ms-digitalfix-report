package cl.duoc.digitalfix.report.web;

import cl.duoc.digitalfix.report.error.AccesoDenegado;
import cl.duoc.digitalfix.report.error.RecursoNoEncontrado;
import cl.duoc.digitalfix.report.error.SolicitudInvalida;
import cl.duoc.digitalfix.report.web.dto.RespuestaError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(SolicitudInvalida.class)
    public ResponseEntity<RespuestaError> manejarSolicitudInvalida(SolicitudInvalida ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RespuestaError.crear("SOLICITUD_INVALIDA", ex.getMessage()));
    }

    @ExceptionHandler(AccesoDenegado.class)
    public ResponseEntity<RespuestaError> manejarAccesoDenegado(AccesoDenegado ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(RespuestaError.crear("ACCESO_DENEGADO", ex.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontrado.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontrado ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RespuestaError.crear("RECURSO_NO_ENCONTRADO", ex.getMessage()));
    }
}