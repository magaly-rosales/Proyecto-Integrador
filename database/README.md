# Base de datos — SeñasApp

Base de datos relacional compartida por el backend Django (módulo Admin)
y el backend Spring Boot (módulo Usuario).

## Requisitos
- PostgreSQL instalado localmente (versión 14 o superior).

## Pasos para crear la base de datos en tu laptop

1. Abre tu cliente de PostgreSQL (pgAdmin o psql por consola).
2. Crea una base de datos nueva llamada: senasapp_db
3. Ejecuta el script ubicado en: database/scripts-sql/schema.sql
   - Desde psql: \i database/scripts-sql/schema.sql
   - Desde pgAdmin: abre el archivo y ejecútalo con el botón "Run".
4. Verifica que se crearon 4 tablas: sena, video_referencia, usuario,
   registro_uso.

## Conexión desde cada backend

### Backend Django (admin-django)
En settings.py, configura:
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': 'senasapp_db',
        'USER': 'postgres',
        'PASSWORD': 'tu_contraseña_local',
        'HOST': 'localhost',
        'PORT': '5432',
    }
}

### Backend Spring Boot (usuario-springboot)
En application.properties, configura:
spring.datasource.url=jdbc:postgresql://localhost:5432/senasapp_db
spring.datasource.username=postgres
spring.datasource.password=tu_contraseña_local
spring.jpa.hibernate.ddl-auto=validate

## Modelo de datos (resumen)
- sena: catálogo de señas reconocidas (nombre, categoría, descripción).
- video_referencia: videos de ejemplo de cada seña (1 seña -> N videos).
- usuario: cuentas del módulo Admin (curador del diccionario).
- registro_uso: estadísticas de uso de la app (sin guardar el contenido
  real de lo traducido, solo cantidad y duración).

Importante: cada integrante corre esto en su propia base de datos LOCAL.
No se comparte una sola base de datos remota por ahora — cada uno prueba
contra su copia local mientras se desarrolla.
