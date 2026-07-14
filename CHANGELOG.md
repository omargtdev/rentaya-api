# Changelog

Todos los cambios relevantes de RentaYa API se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el
proyecto usa [versionado semántico](https://semver.org/lang/es/).

## [Unreleased]

### Agregado

- Autenticación stateless con JWT HS256, login, logout y resolución segura del
  usuario autenticado desde el token.
- Consulta y actualización del perfil actual mediante `/api/users/me`.
- API completa de propiedades con filtros opcionales por distrito y rango de
  precios, administración por propietario, baja lógica y fotos mediante URLs.
- Favoritos persistentes por usuario, con protección contra duplicados y baja
  idempotente.
- Solicitudes de visita con roles, horarios permitidos, prevención de solicitudes
  pendientes duplicadas y gestión por el propietario.
- Conversaciones únicas por propiedad y participantes, listado por usuario y
  mensajes ordenados cronológicamente.
- Catálogo público de distritos.
- Configuración OpenAPI con autenticación Bearer y Swagger UI.
- Datos de desarrollo idempotentes para un propietario, un inquilino y una
  propiedad demo, habilitados por `SEED_DATA_ENABLED`.
- Script `run-local.sh` para iniciar PostgreSQL, ejecutar la API, pruebas y builds.
- Variables de entorno para PostgreSQL, datasource, JWT y datos semilla.
- Healthcheck de PostgreSQL y puerto local configurable, con `5433` como valor
  predeterminado.
- Esquema PostgreSQL de referencia y migración no destructiva desde el modelo
  anterior.
- Prueba de integración del flujo completo del frontend con MockMvc.
- Prueba de regresión con PostgreSQL/Testcontainers para los filtros opcionales de
  propiedades.
- README, contrato funcional y guía de integración para el frontend.

### Cambiado

- El registro normaliza nombres y correos, detecta emails sin distinguir
  mayúsculas y controla carreras de la restricción de unicidad.
- Las validaciones exponen mensajes por campo y los formatos, enums, conflictos y
  permisos inválidos devuelven códigos HTTP controlados en lugar de errores 500.
- La seguridad permite únicamente estado, registro, login, documentación,
  propiedades públicas y catálogos sin JWT; el resto requiere autenticación y, en
  las operaciones sensibles, el rol correspondiente.
- El modelo de propiedad incorpora área y fecha de creación.
- El modelo de visita incorpora nombres de columnas explícitos y fecha de creación.
- Los mensajes ahora pertenecen a una conversación y usan instantes UTC.
- El esquema agrega restricciones de dominio, claves foráneas, unicidad e índices
  para búsquedas, participantes, favoritos, visitas y mensajes.
- La conexión local predeterminada usa PostgreSQL en `localhost:5433` y admite
  configuración externa.
- JPA se ejecuta con `open-in-view` desactivado.
- Las pruebas de registro comprueban el contrato completo y los conflictos de
  unicidad.

### Eliminado

- Documento temporal de pendientes del frontend, porque todos sus puntos fueron
  implementados y su contenido quedó consolidado en el contrato, la guía y este
  changelog.
