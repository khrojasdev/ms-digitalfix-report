CREATE TABLE ordenes_reporte (
                                 orden_id NUMBER PRIMARY KEY,
                                 estado VARCHAR2(50) NOT NULL,
                                 servicio_id NUMBER NOT NULL,
                                 fecha_creacion TIMESTAMP NOT NULL,
                                 fecha_cierre TIMESTAMP,
                                 tiempo_resolucion_minutos NUMBER
);