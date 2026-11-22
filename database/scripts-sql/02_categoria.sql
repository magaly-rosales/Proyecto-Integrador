-- SeñasApp: categorías de señas (compartido por admin Django y usuario Spring Boot)

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria   SERIAL PRIMARY KEY,
    nombre         VARCHAR(50) NOT NULL UNIQUE,
    color          VARCHAR(7)  NOT NULL DEFAULT '#2BBCC9',
    icono          VARCHAR(30) NOT NULL DEFAULT 'hand',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE sena ADD COLUMN IF NOT EXISTS id_categoria INT;
ALTER TABLE sena ADD COLUMN IF NOT EXISTS imagen VARCHAR(255);

-- Unificar mayúsculas del texto antiguo
UPDATE sena
SET categoria = UPPER(LEFT(TRIM(categoria), 1)) || LOWER(SUBSTRING(TRIM(categoria) FROM 2));

-- Una categoría por cada texto existente
INSERT INTO categoria (nombre)
SELECT DISTINCT categoria FROM sena
WHERE categoria IS NOT NULL AND categoria <> ''
ON CONFLICT (nombre) DO NOTHING;

-- Colores e íconos
UPDATE categoria SET color = '#2BBCC9', icono = 'hand'    WHERE nombre = 'Saludos';
UPDATE categoria SET color = '#3B82F6', icono = 'heart'   WHERE nombre = 'Cortesía';
UPDATE categoria SET color = '#22C55E', icono = 'star'    WHERE nombre = 'Necesidades';
UPDATE categoria SET color = '#F59E0B', icono = 'zap'     WHERE nombre = 'Acción';
UPDATE categoria SET color = '#8B5CF6', icono = 'message' WHERE nombre = 'Respuestas';

-- Enlazar cada seña con su categoría
UPDATE sena s
SET id_categoria = c.id_categoria
FROM categoria c
WHERE s.categoria = c.nombre AND s.id_categoria IS NULL;