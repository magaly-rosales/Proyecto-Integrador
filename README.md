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

## Estructura del repositorio
- `backend/` → Lógica de servidor de ambos módulos (Usuario y Administración)
- `frontend/` → Interfaces web de ambos módulo
- `movil/` → Aplicación móvil del módulo Usuario
- `database/` → Esquema y scripts de la base de datos compartida
- `docs/` → Documento del proyecto, diagramas y planificación de sprints

## Estado actual
En desarrollo — Sprint 1 en curso (Historia: reconocimiento de señas
mediante cámara).
