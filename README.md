# SeñasApp — Proyecto Integrador

Traductor de lengua de señas en tiempo real mediante inteligencia artificial.
Proyecto integrador del 4to ciclo — Diseño y Desarrollo de Software (TECSUP).

## Descripción
SeñasApp traduce en tiempo real el lenguaje de señas a texto y voz, usando la
cámara de un celular o laptop, facilitando la comunicación entre personas que
se expresan mediante señas y personas que no conocen este lenguaje.

## Arquitectura
| Módulo | Frontend | Backend |
|---|---|---|
| Usuario | Kotlin (móvil) + React (web) | Spring Boot |
| Administración | React | Django |

Base de datos compartida (relacional) en `/database`.

## Versiones del proyecto

Todo el equipo debe trabajar con exactamente estas versiones, para evitar
errores de compatibilidad entre laptops:

| Tecnología | Versión |
|---|---|
| PostgreSQL | 18 |
| Java (JDK) | 21 (LTS) |
| Spring Boot | 3.3.x |
| Kotlin | 2.0.21 |
| Node.js | 20 LTS |

Antes de empezar a programar, cada integrante debe verificar su versión
instalada con estos comandos:

- java -version        (debe mostrar 21.x)
- kotlinc -version      (debe mostrar 2.0.21)
- node -v               (debe mostrar v20.x)
- psql --version        (debe mostrar 18.x)

Si alguna versión no coincide, desinstalar y reinstalar la versión correcta
antes de continuar con el desarrollo.

## Estructura del repositorio
- `backend/` → Lógica de servidor de ambos módulos (Usuario y Administración)
- `frontend/` → Interfaces web de ambos módulo
- `movil/` → Aplicación móvil del módulo Usuario
- `database/` → Esquema y scripts de la base de datos compartida
- `docs/` → Documento del proyecto, diagramas y planificación de sprints

## Estado actual
En desarrollo — Sprint 1 en curso (Historia: reconocimiento de señas
mediante cámara).
