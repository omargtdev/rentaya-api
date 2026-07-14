# Guia de integracion frontend

Esta guia describe el comportamiento implementado por el backend. Para ejemplos completos de cada modelo y regla de negocio consulta [CONTRATO_BACKEND_API.md](CONTRATO_BACKEND_API.md).

## 1. Iniciar el entorno

Requisitos: Java 17 y Docker con Docker Compose.

```bash
./run-local.sh run
```

Servicios locales:

| Servicio | URL |
| --- | --- |
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI | `http://localhost:8080/v3/api-docs` |
| PostgreSQL | `localhost:5433`, base `rentaya` |

`./run-local.sh run` habilita una semilla idempotente para que el frontend pueda
probar el flujo inmediatamente:

| Rol | Correo | Contrasena |
| --- | --- | --- |
| Propietario | `owner.front@example.com` | `Password1` |
| Inquilino | `tenant.front@example.com` | `Password1` |

La semilla tambien crea una propiedad disponible en Miraflores cuando no existe.
No reemplaza usuarios existentes. Para desactivarla ejecuta
`SEED_DATA_ENABLED=false ./run-local.sh run`. La aplicacion iniciada sin este
script mantiene la semilla desactivada por defecto.

El frontend debe llamar rutas relativas `/api`. En Angular se recomienda este proxy:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

Ejecuta Angular con `ng serve --proxy-config proxy.conf.json`. Así no se requiere configuración CORS para desarrollo.

## 2. Autenticacion

`POST /api/auth/login` devuelve un JWT y el usuario. Todas las rutas protegidas requieren:

```http
Authorization: Bearer <token>
```

El backend obtiene `ownerId`, `tenantId` y `senderId` del JWT. El frontend no debe enviarlos para representar al usuario actual.

Ejemplo de interceptor funcional para Angular:

```ts
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('rentaya_token');
  if (!token || !request.url.startsWith('/api')) return next(request);

  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  }));
};
```

Registra el interceptor con `provideHttpClient(withInterceptors([authInterceptor]))`. Ante un `401`, elimina token y usuario local y redirige al login. `POST /api/auth/logout` responde `204`, pero la sesión es stateless: cerrar sesión consiste principalmente en borrar el JWT del cliente.

En producción define un secreto propio de al menos 32 bytes mediante `JWT_SECRET`. La duración predeterminada es 86400 segundos y se configura con `JWT_EXPIRATION`.

## 3. Endpoints implementados

`Publico` significa que no se envía JWT. Los demás endpoints requieren autenticación.

| Metodo | Ruta | Acceso | Resultado |
| --- | --- | --- | --- |
| `GET` | `/` | Publico | Estado de la API |
| `POST` | `/api/users/register` | Publico | Registra propietario o inquilino |
| `POST` | `/api/auth/login` | Publico | Devuelve JWT y usuario |
| `POST` | `/api/auth/logout` | Autenticado | `204`; el cliente elimina el token |
| `GET` | `/api/users/me` | Autenticado | Perfil actual |
| `PATCH` | `/api/users/me` | Autenticado | Actualiza perfil |
| `GET` | `/api/properties` | Publico | Propiedades disponibles y filtros |
| `GET` | `/api/properties/{id}` | Publico | Detalle de propiedad disponible |
| `GET` | `/api/properties/me` | `PROPIETARIO` | Propiedades propias, incluidas inactivas |
| `POST` | `/api/properties` | `PROPIETARIO` | Crea propiedad |
| `PUT` | `/api/properties/{id}` | Propietario dueño | Reemplaza datos y fotos |
| `DELETE` | `/api/properties/{id}` | Propietario dueño | Baja lógica, estado `Inactivo` |
| `GET` | `/api/favorites` | Autenticado | Favoritos del usuario |
| `POST` | `/api/favorites/{propertyId}` | Autenticado | Agrega favorito |
| `DELETE` | `/api/favorites/{propertyId}` | Autenticado | Quita favorito de forma idempotente |
| `POST` | `/api/visits` | `INQUILINO` | Solicita visita |
| `GET` | `/api/visits/tenant` | `INQUILINO` | Visitas solicitadas |
| `GET` | `/api/visits/owner?status=Pendiente` | `PROPIETARIO` | Visitas recibidas |
| `PATCH` | `/api/visits/{id}/status` | Propietario dueño | Acepta o rechaza visita pendiente |
| `GET` | `/api/conversations` | Autenticado | Conversaciones del participante |
| `POST` | `/api/conversations` | `INQUILINO` | Obtiene o crea conversación |
| `GET` | `/api/conversations/{id}/messages` | Participante | Mensajes en orden cronológico |
| `POST` | `/api/conversations/{id}/messages` | Participante | Envía mensaje |
| `GET` | `/api/catalogs/districts` | Publico | Catálogo de distritos |

Filtros de propiedades:

```text
GET /api/properties?district=Miraflores&minPrice=1000&maxPrice=3000
```

Los listados devuelven arreglos JSON directos, no respuestas paginadas.

## 4. Payloads principales

Registro:

```json
{
  "firstName": "Ana",
  "lastName": "Lopez",
  "email": "ana@example.com",
  "password": "Password1",
  "phone": "987654321",
  "role": "INQUILINO"
}
```

Login:

```json
{
  "email": "ana@example.com",
  "password": "Password1"
}
```

Crear o actualizar propiedad:

```json
{
  "title": "Departamento amoblado",
  "district": "Miraflores",
  "address": "Av. Principal 123",
  "price": 2500,
  "rooms": 2,
  "bathrooms": 1,
  "area": 70,
  "description": "Cerca a parques y transporte.",
  "photos": ["https://example.com/photo-1.jpg"]
}
```

Las fotos se gestionan como una lista de entre 1 y 8 URLs en el JSON de la
propiedad. La API no ofrece almacenamiento binario ni `multipart/form-data`.

Solicitar visita:

```json
{
  "propertyId": 1,
  "date": "2030-01-15",
  "time": "10:00"
}
```

Horarios admitidos: `08:00`, `09:00`, `10:00`, `11:00`, `14:00`, `15:00`, `17:00`, `19:00`. La fecha debe ser hoy o posterior. Solo puede existir una solicitud `Pendiente` por propiedad e inquilino.

Resolver visita:

```json
{ "status": "Aceptada" }
```

Los únicos estados de resolución son `Aceptada` y `Rechazada`.

Crear conversación y enviar mensaje:

```json
{ "propertyId": 1 }
```

```json
{ "content": "Hola, ¿la propiedad sigue disponible?" }
```

El identificador de conversación se serializa como texto con prefijo `c`, por ejemplo `c1`. La primera creación responde `201`; repetirla devuelve la misma conversación con `200`. El frontend puede consultar mensajes periódicamente; WebSocket/SSE no forma parte de esta entrega.

## 5. Flujo de prueba rapido

También se puede probar desde Swagger usando el botón `Authorize` después del login. Este flujo por terminal requiere `curl` y `jq`:

```bash
BASE=http://localhost:8080

curl -sS -X POST "$BASE/api/users/register" \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Paula","lastName":"Owner","email":"owner.front@example.com","password":"Password1","phone":"987654321","role":"PROPIETARIO"}'

curl -sS -X POST "$BASE/api/users/register" \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Luis","lastName":"Tenant","email":"tenant.front@example.com","password":"Password1","phone":"912345678","role":"INQUILINO"}'

OWNER_TOKEN=$(curl -sS -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"owner.front@example.com","password":"Password1"}' | jq -r .token)

TENANT_TOKEN=$(curl -sS -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"tenant.front@example.com","password":"Password1"}' | jq -r .token)

PROPERTY_ID=$(curl -sS -X POST "$BASE/api/properties" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Departamento demo","district":"Miraflores","address":"Av. Demo 123","price":1800,"rooms":2,"bathrooms":1,"area":70,"description":"Propiedad de prueba","photos":["https://example.com/photo.jpg"]}' | jq -r .id)

curl -i -X POST "$BASE/api/favorites/$PROPERTY_ID" \
  -H "Authorization: Bearer $TENANT_TOKEN"

curl -sS -X POST "$BASE/api/visits" \
  -H "Authorization: Bearer $TENANT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"propertyId\":$PROPERTY_ID,\"date\":\"2030-01-15\",\"time\":\"10:00\"}"

CONVERSATION_ID=$(curl -sS -X POST "$BASE/api/conversations" \
  -H "Authorization: Bearer $TENANT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"propertyId\":$PROPERTY_ID}" | jq -r .id)

curl -sS -X POST "$BASE/api/conversations/$CONVERSATION_ID/messages" \
  -H "Authorization: Bearer $TENANT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"content":"Hola, deseo coordinar una visita."}'
```

Los usuarios de desarrollo ya existen al iniciar con `./run-local.sh run`; sus
intentos de registro repetidos devolveran `409`, lo cual no impide continuar con
el login. Reemplaza la fecha por una fecha futura cuando corresponda.

## 6. Errores y estados

Error general:

```json
{ "error": "Mensaje legible para el usuario" }
```

Errores de campos:

```json
{
  "email": "Ingresa un correo válido",
  "phone": "Ingresa un teléfono válido"
}
```

Tratamiento recomendado en frontend:

| Estado | Acción |
| --- | --- |
| `400` | Mostrar errores de formulario o regla de negocio |
| `401` | Limpiar sesión y redirigir a login |
| `403` | Mostrar acceso no permitido |
| `404` | Mostrar recurso no disponible |
| `409` | Mostrar duplicado, por ejemplo email, favorito o visita pendiente |
| `500` | Mostrar un error general y registrar el incidente |

Fechas de calendario usan `YYYY-MM-DD`, horas `HH:mm` e instantes de auditoría ISO-8601 UTC, por ejemplo `2026-07-14T03:30:00Z`.

## 7. Persistencia SQL

Para una base vacía, [`schema.sql`](../schema.sql) contiene las siete tablas, claves foráneas, restricciones e índices. La aplicación usa `spring.jpa.hibernate.ddl-auto=update` en desarrollo.

Si existe una base creada con el modelo anterior, aplica [`migration_v2_contract.sql`](../migration_v2_contract.sql) antes de arrancar esta versión:

```bash
docker compose cp migration_v2_contract.sql postgres:/tmp/migration.sql
docker compose exec -T postgres \
  psql -v ON_ERROR_STOP=1 -U postgres -d rentaya -f /tmp/migration.sql
```

La migración conserva usuarios y datos, crea conversaciones, adapta mensajes y añade restricciones e índices contractuales.
