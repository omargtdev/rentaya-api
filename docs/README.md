# Documentacion de RentaYa API

- [Contrato funcional](CONTRATO_BACKEND_API.md): fuente de verdad de modelos, reglas y respuestas esperadas.
- [Guia de integracion frontend](GUIA_FRONTEND_API.md): puesta en marcha, autenticacion, endpoints y flujo de prueba.
- [`CHANGELOG.md`](../CHANGELOG.md): historial consolidado de cambios de la API.
- [`schema.sql`](../schema.sql): esquema completo para una base PostgreSQL nueva.
- [`migration_v2_contract.sql`](../migration_v2_contract.sql): migracion desde el esquema original sin eliminar usuarios ni datos de dominio.

La especificacion OpenAPI ejecutable queda disponible al iniciar la aplicacion:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
