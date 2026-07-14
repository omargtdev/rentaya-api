# Contrato de RentaYa API

> **Estado:** implementado. Incluye autenticación JWT, perfil, propiedades,
> favoritos, visitas, conversaciones, mensajes y catálogo de distritos. Las fotos
> se representan mediante URLs, el chat usa HTTP/polling y el logout es stateless.
> Consulta [GUIA_FRONTEND_API.md](GUIA_FRONTEND_API.md) para iniciar el entorno y
> ejecutar el flujo de prueba.

Este documento describe el contrato implementado por el backend para las vistas
del frontend Angular.

Fuente revisada:

- `src/app/app.routes.ts`
- `src/app/models/*.ts`
- `src/app/services/*.ts`
- vistas de registro, login, perfil, propiedades, favoritos, visitas y mensajes

## 1. Configuración general

### Base URL local

El frontend llama rutas relativas bajo `/api`.

```text
/api
```

En desarrollo, Angular reenvía esas llamadas al backend mediante `proxy.conf.json`:

```text
http://localhost:8080
```

### Autenticación requerida

La API usa autenticación stateless con JWT:

```http
Authorization: Bearer <jwt>
```

El backend debe derivar el usuario autenticado desde el token. No debe confiar en `ownerId`, `tenantId` o `senderId` enviados por el cliente para acciones del usuario actual.

### Formato de errores

Errores por campo:

```json
{
  "email": "Ingresa un correo válido",
  "phone": "Ingresa un teléfono válido"
}
```

Error general:

```json
{
  "error": "Mensaje legible para el usuario"
}
```

### Códigos HTTP esperados

| Código | Uso |
| --- | --- |
| `200 OK` | Lectura o actualización exitosa |
| `201 Created` | Creación exitosa |
| `204 No Content` | Eliminación exitosa sin body |
| `400 Bad Request` | Validación incorrecta o regla de negocio incumplida |
| `401 Unauthorized` | Sin token o token inválido |
| `403 Forbidden` | Usuario autenticado sin permiso |
| `404 Not Found` | Recurso no encontrado |
| `409 Conflict` | Conflicto de unicidad o duplicado |
| `500 Internal Server Error` | Error no controlado; no usar para validaciones esperadas |

## 2. Modelos de dominio

### UserRole

```ts
type UserRole = 'PROPIETARIO' | 'INQUILINO';
```

### UserResponse

```ts
interface UserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: 'PROPIETARIO' | 'INQUILINO';
}
```

Reglas:

- `firstName`: requerido.
- `lastName`: requerido.
- `email`: requerido, formato email, único.
- `phone`: requerido, exactamente 9 dígitos.
- `role`: solo `PROPIETARIO` o `INQUILINO`.
- Nunca devolver `password`.

### PropertyStatus

```ts
type PropertyStatus = 'Disponible' | 'Inactivo';
```

### PropertyResponse

```ts
interface PropertyResponse {
  id: number;
  ownerId: number;
  ownerName: string;
  title: string;
  district: string;
  address: string;
  price: number;
  rooms: number;
  bathrooms: number;
  area?: number;
  description: string;
  photos: string[];
  status: 'Disponible' | 'Inactivo';
  createdAt: string;
}
```

Reglas:

- `title`: requerido.
- `district`: requerido.
- `address`: requerido.
- `price`: requerido, mayor a 0.
- `rooms`: requerido, mínimo 1.
- `bathrooms`: requerido, mínimo 1.
- `area`: opcional.
- `description`: requerido.
- `photos`: requerido, entre 1 y 8 imágenes.
- `ownerId`, `ownerName`, `status` y `createdAt` los asigna el backend.
- El listado público debe devolver solo propiedades `Disponible`.

### VisitStatus

```ts
type VisitStatus = 'Pendiente' | 'Aceptada' | 'Rechazada';
```

### VisitResponse

```ts
interface VisitResponse {
  id: number;
  propertyId: number;
  propertyTitle: string;
  tenantId: number;
  tenantName: string;
  ownerId: number;
  date: string;
  time: string;
  status: 'Pendiente' | 'Aceptada' | 'Rechazada';
  createdAt: string;
}
```

Reglas:

- `date`: requerido, no puede ser fecha pasada.
- `time`: requerido.
- Horarios usados por la UI: `08:00`, `09:00`, `10:00`, `11:00`, `14:00`, `15:00`, `17:00`, `19:00`.
- Solo inquilinos solicitan visitas.
- Solo propietarios gestionan solicitudes de sus propiedades.
- No permitir más de una visita `Pendiente` por `propertyId + tenantId`.

### ConversationResponse

```ts
interface ConversationResponse {
  id: string;
  propertyId: number;
  propertyTitle: string;
  ownerId: number;
  ownerName: string;
  tenantId: number;
  tenantName: string;
  lastMessage: string;
  lastMessageAt: string;
}
```

### ChatMessageResponse

```ts
interface ChatMessageResponse {
  id: number;
  conversationId: string;
  senderId: number;
  senderName: string;
  content: string;
  sentAt: string;
}
```

Reglas:

- Una conversación pertenece a una propiedad, un propietario y un inquilino.
- Evitar conversaciones duplicadas para el mismo `propertyId + ownerId + tenantId`.
- `content`: requerido, no vacío, máximo 500 caracteres.
- `senderId` y `senderName` se derivan del usuario autenticado.

## 3. API implementada

## 3.1 Registro de usuarios

Vista frontend: `/register`

### POST `/api/users/register`

Request:

```json
{
  "firstName": "Omar",
  "lastName": "Gutierrez",
  "email": "omar@example.com",
  "password": "Password1",
  "phone": "987654321",
  "role": "INQUILINO"
}
```

Validaciones:

- `firstName`: requerido.
- `lastName`: requerido.
- `email`: requerido, formato email, único.
- `password`: mínimo 8 caracteres, al menos 1 mayúscula y 1 número.
- `phone`: exactamente 9 dígitos.
- `role`: `PROPIETARIO` o `INQUILINO`.

Response `201 Created`:

```json
{
  "id": 1,
  "firstName": "Omar",
  "lastName": "Gutierrez",
  "email": "omar@example.com",
  "phone": "987654321",
  "role": "INQUILINO"
}
```

Errores:

```http
400 Bad Request
```

```json
{
  "password": "Mínimo 8 caracteres, 1 mayúscula y 1 número"
}
```

```http
409 Conflict
```

```json
{
  "email": "El correo ya está registrado"
}
```

## 3.2 Login y sesión

Vista frontend: `/login`

### POST `/api/auth/login`

Request:

```json
{
  "email": "emir.sanchez@correo.com",
  "password": "Demo1234"
}
```

Response `200 OK`:

```json
{
  "token": "jwt-token",
  "user": {
    "id": 2,
    "firstName": "Emir",
    "lastName": "Sánchez",
    "email": "emir.sanchez@correo.com",
    "phone": "999888777",
    "role": "PROPIETARIO"
  }
}
```

Errores:

- `400`: formato inválido.
- `401`: credenciales incorrectas.

### GET `/api/users/me`

Headers:

```http
Authorization: Bearer <jwt>
```

Response `200 OK`:

```json
{
  "id": 2,
  "firstName": "Emir",
  "lastName": "Sánchez",
  "email": "emir.sanchez@correo.com",
  "phone": "999888777",
  "role": "PROPIETARIO"
}
```

Errores:

- `401`: token ausente o inválido.

### POST `/api/auth/logout`

El endpoint valida el JWT y responde sin body. Como la sesión es stateless, el
cliente también debe eliminar su copia local del token.

Response:

```http
204 No Content
```

## 3.3 Perfil

Vista frontend: `/profile`

### PATCH `/api/users/me`

Headers:

```http
Authorization: Bearer <jwt>
```

Request:

```json
{
  "firstName": "Ana",
  "lastName": "López",
  "email": "ana.lopez@correo.com",
  "phone": "987654321"
}
```

Response `200 OK`:

```json
{
  "id": 10,
  "firstName": "Ana",
  "lastName": "López",
  "email": "ana.lopez@correo.com",
  "phone": "987654321",
  "role": "INQUILINO"
}
```

Errores:

- `400`: campos inválidos.
- `401`: no autenticado.
- `409`: email ya usado por otro usuario.

## 3.4 Propiedades

Vistas frontend:

- `/properties`
- `/properties/new`
- `/properties/:id`
- `/properties/:id/edit`

### GET `/api/properties`

Lista propiedades disponibles.

Query params opcionales:

```text
district=Miraflores
minPrice=1000
maxPrice=3000
```

Request ejemplo:

```http
GET /api/properties?district=Miraflores&minPrice=1000&maxPrice=3000
```

Response `200 OK`:

```json
[
  {
    "id": 1,
    "ownerId": 2,
    "ownerName": "Emir Sánchez",
    "title": "Departamento amoblado en Miraflores",
    "district": "Miraflores",
    "address": "Av. Principal 123",
    "price": 2500,
    "rooms": 2,
    "bathrooms": 1,
    "area": 70,
    "description": "Propiedad disponible, bien ubicada.",
    "photos": [
      "https://example.com/properties/1/photo-1.jpg"
    ],
    "status": "Disponible",
    "createdAt": "2026-06-01"
  }
]
```

Reglas:

- Filtrar solo `status = Disponible`.
- Aplicar filtros si llegan.
- Orden: más recientes primero.

### GET `/api/properties/{id}`

Response `200 OK`:

```json
{
  "id": 1,
  "ownerId": 2,
  "ownerName": "Emir Sánchez",
  "title": "Departamento amoblado en Miraflores",
  "district": "Miraflores",
  "address": "Av. Principal 123",
  "price": 2500,
  "rooms": 2,
  "bathrooms": 1,
  "area": 70,
  "description": "Propiedad disponible, bien ubicada.",
  "photos": [
    "https://example.com/properties/1/photo-1.jpg"
  ],
  "status": "Disponible",
  "createdAt": "2026-06-01"
}
```

Errores:

- `404`: propiedad no encontrada.

### POST `/api/properties`

Solo `PROPIETARIO`.

Headers:

```http
Authorization: Bearer <jwt>
```

Request:

```json
{
  "title": "Departamento amoblado",
  "district": "Miraflores",
  "address": "Av. Principal 123",
  "price": 2500,
  "rooms": 2,
  "bathrooms": 1,
  "area": 70,
  "description": "Características, servicios y reglas del alquiler.",
  "photos": [
    "https://example.com/properties/tmp/photo-1.jpg"
  ]
}
```

Response `201 Created`:

```json
{
  "id": 7,
  "ownerId": 2,
  "ownerName": "Emir Sánchez",
  "title": "Departamento amoblado",
  "district": "Miraflores",
  "address": "Av. Principal 123",
  "price": 2500,
  "rooms": 2,
  "bathrooms": 1,
  "area": 70,
  "description": "Características, servicios y reglas del alquiler.",
  "photos": [
    "https://example.com/properties/tmp/photo-1.jpg"
  ],
  "status": "Disponible",
  "createdAt": "2026-07-14"
}
```

Errores:

- `400`: validación incorrecta.
- `401`: no autenticado.
- `403`: usuario no es propietario.

### PUT `/api/properties/{id}`

Solo el propietario dueño.

Headers:

```http
Authorization: Bearer <jwt>
```

Request:

```json
{
  "title": "Departamento actualizado",
  "district": "Miraflores",
  "address": "Av. Principal 123",
  "price": 2600,
  "rooms": 2,
  "bathrooms": 1,
  "area": 70,
  "description": "Descripción actualizada.",
  "photos": [
    "https://example.com/properties/1/photo-1.jpg"
  ]
}
```

Response `200 OK`: `PropertyResponse`.

Errores:

- `400`: validación incorrecta.
- `401`: no autenticado.
- `403`: no es dueño de la propiedad.
- `404`: propiedad no encontrada.

### DELETE `/api/properties/{id}`

Solo el propietario dueño.

Headers:

```http
Authorization: Bearer <jwt>
```

Response:

```http
204 No Content
```

Regla:

- Hacer eliminación lógica cambiando `status` a `Inactivo` para no romper visitas, favoritos o conversaciones históricas.

### GET `/api/properties/me`

Headers:

```http
Authorization: Bearer <jwt>
```

Response `200 OK`: arreglo de `PropertyResponse` del propietario autenticado.

## 3.5 Fotos de propiedades

La API persiste las fotos como URLs. No existe un endpoint de carga binaria.
`POST /api/properties` y `PUT /api/properties/{id}` aceptan:

```json
{
  "photos": [
    "https://example.com/photo.jpg"
  ]
}
```

Reglas:

- Cada elemento debe ser una URL no vacía de hasta 255 caracteres.
- Se requiere al menos una foto.
- Máximo 8 fotos por propiedad.

## 3.6 Favoritos

Vista frontend: `/favorites`

Los favoritos se persisten por usuario autenticado.

### GET `/api/favorites`

Headers:

```http
Authorization: Bearer <jwt>
```

Response `200 OK`:

```json
[
  {
    "id": 1,
    "ownerId": 2,
    "ownerName": "Emir Sánchez",
    "title": "Departamento amoblado en Miraflores",
    "district": "Miraflores",
    "address": "Av. Principal 123",
    "price": 2500,
    "rooms": 2,
    "bathrooms": 1,
    "area": 70,
    "description": "Propiedad disponible, bien ubicada.",
    "photos": [
      "https://example.com/properties/1/photo-1.jpg"
    ],
    "status": "Disponible",
    "createdAt": "2026-06-01"
  }
]
```

### POST `/api/favorites/{propertyId}`

Headers:

```http
Authorization: Bearer <jwt>
```

Response:

```http
201 Created
```

Errores:

- `401`: no autenticado.
- `404`: propiedad no encontrada.
- `409`: favorito ya existe.

### DELETE `/api/favorites/{propertyId}`

Headers:

```http
Authorization: Bearer <jwt>
```

Response:

```http
204 No Content
```

## 3.7 Solicitudes de visita

Vistas frontend:

- detalle de propiedad: solicitar visita;
- `/visits`: gestión de solicitudes recibidas por propietario.

### POST `/api/visits`

Solo `INQUILINO`.

Headers:

```http
Authorization: Bearer <jwt>
```

Request:

```json
{
  "propertyId": 1,
  "date": "2026-07-20",
  "time": "10:00"
}
```

Response `201 Created`:

```json
{
  "id": 10,
  "propertyId": 1,
  "propertyTitle": "Departamento amoblado en Miraflores",
  "tenantId": 10,
  "tenantName": "Ana López",
  "ownerId": 2,
  "date": "2026-07-20",
  "time": "10:00",
  "status": "Pendiente",
  "createdAt": "2026-07-14T03:30:00Z"
}
```

Reglas:

- `tenantId` y `tenantName` salen del token.
- `ownerId` y `propertyTitle` salen de la propiedad.
- No permitir fecha pasada.
- No permitir más de una solicitud `Pendiente` para la misma propiedad e inquilino.
- No permitir que el dueño solicite visita a su propia propiedad.

Errores:

```http
400 Bad Request
```

```json
{
  "error": "No se pueden seleccionar fechas anteriores al día actual."
}
```

```http
409 Conflict
```

```json
{
  "error": "Ya tienes una solicitud pendiente para esta propiedad."
}
```

### GET `/api/visits/owner`

Lista solicitudes recibidas por el propietario autenticado.

Headers:

```http
Authorization: Bearer <jwt>
```

Query param opcional:

```text
status=Pendiente
```

Response `200 OK`:

```json
[
  {
    "id": 1,
    "propertyId": 1,
    "propertyTitle": "Departamento en Miraflores",
    "tenantId": 10,
    "tenantName": "Ana López",
    "ownerId": 2,
    "date": "2026-07-20",
    "time": "10:00",
    "status": "Pendiente",
    "createdAt": "2026-07-14T03:30:00Z"
  }
]
```

Reglas:

- Solo propietario.
- Orden: `createdAt` descendente.

### GET `/api/visits/tenant`

Lista solicitudes creadas por el inquilino autenticado.

Headers:

```http
Authorization: Bearer <jwt>
```

Response `200 OK`: arreglo de `VisitResponse`.

### PATCH `/api/visits/{id}/status`

Solo propietario dueño de la propiedad asociada.

Headers:

```http
Authorization: Bearer <jwt>
```

Request:

```json
{
  "status": "Aceptada"
}
```

Valores permitidos:

- `Aceptada`
- `Rechazada`

Response `200 OK`:

```json
{
  "id": 1,
  "propertyId": 1,
  "propertyTitle": "Departamento en Miraflores",
  "tenantId": 10,
  "tenantName": "Ana López",
  "ownerId": 2,
  "date": "2026-07-20",
  "time": "10:00",
  "status": "Aceptada",
  "createdAt": "2026-07-14T03:30:00Z"
}
```

Errores:

- `400`: estado inválido o solicitud ya resuelta.
- `403`: no es propietario dueño.
- `404`: solicitud no existe.

## 3.8 Conversaciones y mensajes

Vista frontend: `/messages`

El botón "Contactar propietario" en detalle de propiedad debe obtener o crear una conversación y redirigir a `/messages?conversation=<id>`.

### GET `/api/conversations`

Headers:

```http
Authorization: Bearer <jwt>
```

Response `200 OK`:

```json
[
  {
    "id": "c1",
    "propertyId": 1,
    "propertyTitle": "Departamento amoblado en Miraflores",
    "ownerId": 2,
    "ownerName": "Emir Sánchez",
    "tenantId": 10,
    "tenantName": "Ana López",
    "lastMessage": "Perfecto, ¿incluye mantenimiento?",
    "lastMessageAt": "2026-06-25T10:15:00Z"
  }
]
```

Reglas:

- Devolver solo conversaciones donde el usuario autenticado sea propietario o inquilino.
- Ordenar por `lastMessageAt` descendente.

### POST `/api/conversations`

Obtiene o crea conversación para una propiedad.

Headers:

```http
Authorization: Bearer <jwt>
```

Request:

```json
{
  "propertyId": 1
}
```

Response:

- `200 OK` si ya existía.
- `201 Created` si se creó.

Body:

```json
{
  "id": "c1",
  "propertyId": 1,
  "propertyTitle": "Departamento amoblado en Miraflores",
  "ownerId": 2,
  "ownerName": "Emir Sánchez",
  "tenantId": 10,
  "tenantName": "Ana López",
  "lastMessage": "",
  "lastMessageAt": "2026-07-14T03:30:00Z"
}
```

Reglas:

- `tenantId` y `tenantName` salen del token.
- `ownerId`, `ownerName` y `propertyTitle` salen de la propiedad.
- No permitir iniciar conversación con propiedad propia.
- Evitar duplicados.

### GET `/api/conversations/{conversationId}/messages`

Headers:

```http
Authorization: Bearer <jwt>
```

Response `200 OK`:

```json
[
  {
    "id": 1,
    "conversationId": "c1",
    "senderId": 10,
    "senderName": "Ana López",
    "content": "Hola, ¿la propiedad sigue disponible?",
    "sentAt": "2026-06-25T10:10:00Z"
  }
]
```

Reglas:

- Solo participantes de la conversación.
- Ordenar por `sentAt` ascendente.

### POST `/api/conversations/{conversationId}/messages`

Headers:

```http
Authorization: Bearer <jwt>
```

Request:

```json
{
  "content": "Hola, ¿la propiedad sigue disponible?"
}
```

Response `201 Created`:

```json
{
  "id": 6,
  "conversationId": "c1",
  "senderId": 10,
  "senderName": "Ana López",
  "content": "Hola, ¿la propiedad sigue disponible?",
  "sentAt": "2026-07-14T03:30:00Z"
}
```

Reglas:

- `content`: requerido, no vacío, máximo 500 caracteres.
- Solo participantes de la conversación.
- Actualizar `lastMessage` y `lastMessageAt` de la conversación.

La API usa HTTP y no expone WebSocket ni SSE. El frontend puede actualizar los
mensajes mediante polling.

## 3.9 Catálogos

La vista de listado usa este catálogo de distritos:

```json
[
  "Miraflores",
  "Surco",
  "San Borja",
  "Lince",
  "Jesús María",
  "Barranco"
]
```

### GET `/api/catalogs/districts`

Response `200 OK`:

```json
[
  "Miraflores",
  "Surco",
  "San Borja",
  "Lince",
  "Jesús María",
  "Barranco"
]
```

## 4. Matriz de vistas contra endpoints

| Vista frontend | Ruta | Endpoints backend requeridos |
| --- | --- | --- |
| Registro | `/register` | `POST /api/users/register` |
| Login | `/login` | `POST /api/auth/login`, `GET /api/users/me` |
| Perfil | `/profile` | `GET /api/users/me`, `PATCH /api/users/me` |
| Listado de propiedades | `/properties` | `GET /api/properties` |
| Publicar propiedad | `/properties/new` | `POST /api/properties` |
| Detalle de propiedad | `/properties/:id` | `GET /api/properties/{id}`, favoritos, visitas, conversaciones |
| Editar propiedad | `/properties/:id/edit` | `GET /api/properties/{id}`, `PUT /api/properties/{id}` |
| Favoritos | `/favorites` | `GET /api/favorites`, `POST /api/favorites/{propertyId}`, `DELETE /api/favorites/{propertyId}` |
| Visitas | `/visits` | `GET /api/visits/owner`, `PATCH /api/visits/{id}/status` |
| Mensajes | `/messages` | `GET /api/conversations`, `POST /api/conversations`, `GET/POST /api/conversations/{id}/messages` |

## 5. Garantías del contrato

- No devolver contraseñas en ningún response.
- Las rutas protegidas validan JWT.
- Las acciones por rol devuelven `403` si no corresponde.
- Las validaciones esperadas devuelven `400`, no `500`.
- Los errores por campo usan nombres iguales al request.
- `role` inválido devuelve `400`.
- `status` inválido devuelve `400`.
- Los IDs de usuario se derivan del token.
- El propietario solo modifica sus propias propiedades.
- El inquilino no puede solicitar visita a su propia propiedad.
- No hay favoritos duplicados por usuario.
- No hay conversaciones duplicadas por propiedad + propietario + inquilino.
- Los mensajes respetan el límite de 500 caracteres.
- El backend mantiene consistencia si una propiedad se elimina o se marca como `Inactivo`.
