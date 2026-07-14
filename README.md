# Rentaya API

Backend REST de Rentaya construido con Java 17, Spring Boot 3.5, Maven y PostgreSQL 17.

## Requisitos

- Java 17
- Docker con Docker Compose

El Maven Wrapper incluido descarga Maven y las dependencias del proyecto. En este entorno tambien hay un JDK y Docker Compose locales bajo `.tools/`; esa carpeta no se versiona.

## Ejecucion local

El script `run-local.sh` levanta PostgreSQL en `localhost:5433` y ejecuta la API en `http://localhost:8080`:

```bash
./run-local.sh run
```

Comandos disponibles:

```bash
./run-local.sh db-up
./run-local.sh test
./run-local.sh build
./run-local.sh db-down
```

La configuracion predeterminada es:

| Variable | Valor predeterminado |
| --- | --- |
| `POSTGRES_PORT` | `5433` |
| `POSTGRES_DB` | `rentaya` |
| `POSTGRES_USER` | `postgres` |
| `POSTGRES_PASSWORD` | `rootroot` |
| `DB_URL` | `jdbc:postgresql://localhost:5433/rentaya` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `rootroot` |
| `JWT_SECRET` | secreto local de desarrollo |
| `JWT_EXPIRATION` | `86400` segundos |
| `SEED_DATA_ENABLED` | `true` con `./run-local.sh run`; `false` al iniciar Spring directamente |

Las variables `POSTGRES_*` configuran el contenedor y las variables `DB_*` configuran Spring Boot.

Para integracion local, el script crea de forma idempotente una propiedad demo y
dos usuarios: `owner.front@example.com` y `tenant.front@example.com`, ambos con
contrasena `Password1`. Usa `SEED_DATA_ENABLED=false ./run-local.sh run` para
iniciar sin datos semilla.

## Endpoints disponibles

- `GET /`: estado de la API.
- `/api/users` y `/api/auth`: registro, login JWT y perfil.
- `/api/properties`: listado, filtros y gestión de propiedades.
- `/api/favorites`: favoritos persistentes por usuario.
- `/api/visits`: solicitudes y gestión de visitas.
- `/api/conversations`: conversaciones y mensajes.
- `/api/catalogs/districts`: catálogo público de distritos.
- `GET /swagger-ui.html`: documentacion interactiva OpenAPI.

Ejemplo de registro:

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Omar","lastName":"Gutierrez","email":"omar@example.com","password":"Password1","phone":"987654321","role":"INQUILINO"}'
```

## Documentacion

- [Changelog](CHANGELOG.md)
- [Indice de documentacion](docs/README.md)
- [Contrato backend](docs/CONTRATO_BACKEND_API.md)
- [Guia de integracion y pruebas del frontend](docs/GUIA_FRONTEND_API.md)
- [Esquema PostgreSQL](schema.sql)
- [Migracion desde el esquema anterior](migration_v2_contract.sql)
