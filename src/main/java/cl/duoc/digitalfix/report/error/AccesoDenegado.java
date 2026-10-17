package cl.duoc.digitalfix.report.error;

public class AccesoDenegado extends RuntimeException {
    public AccesoDenegado(String mensaje) {
        super(mensaje);
    }
}