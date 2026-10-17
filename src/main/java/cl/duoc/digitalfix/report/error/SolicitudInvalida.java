package cl.duoc.digitalfix.report.error;

public class SolicitudInvalida extends RuntimeException {
    public SolicitudInvalida(String mensaje) {
        super(mensaje);
    }
}