CREATE TABLE sena (
    id_sena SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    descripcion TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE video_referencia (
    id_video SERIAL PRIMARY KEY,
    id_sena INT NOT NULL REFERENCES sena(id_sena),
    url_video VARCHAR(255) NOT NULL,
    formato VARCHAR(10) DEFAULT 'mp4',
    tamano_mb DECIMAL(5,2),
    fecha_subida TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) DEFAULT 'curador'
);

CREATE TABLE registro_uso (
    id_registro SERIAL PRIMARY KEY,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duracion_segundos INT,
    cantidad_senas_traducidas INT,
    plataforma VARCHAR(20)
);
