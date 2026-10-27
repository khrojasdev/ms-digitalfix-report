-- El modelo de lectura no guardaba la empresa: los indicadores sumaban las
-- ordenes de todas las empresas. Las filas anteriores quedan sin empresa y no
-- se cuentan en ningun reporte.
ALTER TABLE ordenes_reporte ADD empresa_id NUMBER;

CREATE INDEX idx_reporte_empresa_fecha ON ordenes_reporte (empresa_id, fecha_creacion);
