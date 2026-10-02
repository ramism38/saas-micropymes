# SaaS Micropymes

Backend de un SaaS orientado a **micropymes y autónomos** para centralizar el seguimiento comercial de clientes, oportunidades, presupuestos y tareas de seguimiento, reduciendo la pérdida de oportunidades por falta de organización o seguimiento.

> **Estado actual:** backend del MVP completado. La interfaz web se desarrollará en una fase posterior.

---

## Objetivo del proyecto

El producto busca resolver un problema sencillo: muchas micropymes gestionan contactos, presupuestos y seguimientos mediante herramientas dispersas, hojas de cálculo o memoria personal.

El MVP centraliza el flujo comercial en una única aplicación:

**Cliente → Oportunidad → Presupuesto → Seguimiento → Cierre**

Además, incorpora priorización automática, un dashboard diario y un historial de actividad para ayudar a decidir qué oportunidades requieren atención.

---

## Stack tecnológico

| Área | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.1 |
| API | REST / JSON |
| Seguridad | Spring Security + HttpSession + CSRF |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | PostgreSQL 17 |
| Migraciones | Flyway |
| Documentación API | OpenAPI / Swagger UI |
| Build | Maven Wrapper |
| Contenedores | Docker Compose |
| Testing | JUnit, Mockito, Spring Boot Test, Testcontainers |
| Arquitectura | Monolito modular |

---

## Funcionalidades implementadas

- Registro y login mediante sesión HTTP.
- Gestión de organizaciones y miembros.
- Roles `OWNER` y `MEMBER`.
- Gestión de clientes.
- Gestión de oportunidades comerciales.
- Gestión de presupuestos.
- Gestión de follow-ups.
- Historial de actividades.
- Dashboard diario.
- Priorización automática de oportunidades.
- Expiración automática de presupuestos.
- Archivado y restauración de clientes y oportunidades.
- Aislamiento multi-tenant.
- Control de concurrencia mediante optimistic locking y bloqueos pesimistas en reglas críticas.
- API documentada mediante OpenAPI.
- Manejo uniforme de errores mediante `ApiError`.
- Suite de tests unitarios, integración y end-to-end.

---

## Arquitectura

El backend utiliza un **monolito modular** organizado por dominio.

```text
backend/
└── src/
    ├── main/
    │   ├── java/com/micropymes/backend/
    │   │   ├── activity/
    │   │   ├── auth/
    │   │   ├── common/
    │   │   ├── customer/
    │   │   ├── dashboard/
    │   │   ├── followup/
    │   │   ├── opportunity/
    │   │   ├── organization/
    │   │   ├── priority/
    │   │   ├── quote/
    │   │   └── user/
    │   └── resources/
    │       ├── db/migration/
    │       ├── application.yml
    │       └── application-local.yml
    └── test/
```

Cada módulo mantiene, cuando corresponde, sus propias capas:

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
PostgreSQL
```

Las reglas de negocio se concentran principalmente en la capa `service`.

---

## Modelo de datos

El modelo principal está formado por ocho tablas:

| Tabla | Responsabilidad |
|---|---|
| `organizations` | Empresas o espacios de trabajo |
| `users` | Usuarios registrados |
| `organization_members` | Relación usuario-organización y rol |
| `customers` | Clientes comerciales |
| `opportunities` | Oportunidades de venta |
| `quotes` | Presupuestos |
| `follow_ups` | Tareas de seguimiento |
| `activities` | Historial de actividad |

Las migraciones se encuentran en:

```text
backend/src/main/resources/db/migration/
```

Flyway crea y versiona el esquema y Hibernate se ejecuta con:

```yaml
ddl-auto: validate
```

por lo que el esquema lo controla Flyway y Hibernate únicamente comprueba que las entidades sean compatibles.

---

## Multi-tenancy

El MVP utiliza una única base de datos y un único esquema compartido.

Cada recurso comercial contiene un:

```text
organization_id
```

La organización forma parte también de las consultas de repositorio, por ejemplo:

```java
findByIdAndOrganization_Id(resourceId, organizationId)
```

Esto evita acceder a un recurso únicamente conociendo su UUID.

La base de datos añade además restricciones y claves foráneas compuestas para impedir asociaciones entre recursos de organizaciones diferentes.

Los tests de integración verifican que:

```text
Usuario de organización B
        ↓
intenta acceder a recursos de organización A
        ↓
404
```

sin revelar información del tenant ajeno.

---

## Seguridad

La autenticación utiliza **sesiones HTTP de servidor**, no JWT.

El flujo utiliza:

```text
JSESSIONID
XSRF-TOKEN
X-XSRF-TOKEN
```

Características implementadas:

- contraseñas almacenadas mediante BCrypt;
- cookies `HttpOnly` para sesión;
- protección CSRF para operaciones de escritura;
- CORS configurado para el frontend local;
- regeneración del identificador de sesión tras autenticación para mitigar session fixation;
- respuestas JSON uniformes para `401` y `403`;
- ocultación de recursos pertenecientes a otros tenants.

La sesión se crea después de un registro o login correcto y el `SecurityContext` se persiste mediante `HttpSessionSecurityContextRepository`.

---

## Roles y miembros

El MVP dispone de dos roles:

```text
OWNER
MEMBER
```

`OWNER` puede administrar la organización y sus miembros.

`MEMBER` puede trabajar con los recursos comerciales de la organización.

La organización nunca puede quedarse sin un `OWNER` activo. Esta regla se protege tanto mediante lógica de negocio como mediante un bloqueo pesimista sobre la organización durante operaciones administrativas concurrentes.

---

## Oportunidades

Estados disponibles:

```text
NEW
CONTACTED
PROPOSAL_SENT
NEGOTIATION
WON
LOST
```

Reglas destacadas:

- una oportunidad pertenece siempre a un cliente de la misma organización;
- un cliente archivado no puede recibir nuevas oportunidades;
- una oportunidad no puede restaurarse mientras su cliente permanezca archivado;
- `WON` y `LOST` establecen `closedAt`;
- reabrir limpia los datos de cierre;
- una oportunidad `WON` con un presupuesto aceptado no se puede reabrir;
- cerrar o archivar una oportunidad cancela sus follow-ups pendientes;
- `estimatedValue` y `currency` se mantienen coherentes;
- el `PATCH` permite actualizar parcialmente valor o moneda conservando el otro valor existente.

---

## Presupuestos

Estados disponibles:

```text
DRAFT
SENT
ACCEPTED
REJECTED
EXPIRED
```

Flujo principal:

```text
DRAFT
  ↓ send
SENT
  ├── accept → ACCEPTED → Opportunity WON
  ├── reject → REJECTED
  └── expiry → EXPIRED
```

Solo puede existir un presupuesto `ACCEPTED` por oportunidad. La regla está protegida en el backend y también mediante un índice único parcial en PostgreSQL.

Una quote expirada no puede aceptarse aunque el scheduler todavía no haya actualizado su estado.

---

## Follow-ups

Tipos:

```text
CALL
EMAIL
WHATSAPP
MEETING
OTHER
```

Estados:

```text
PENDING
COMPLETED
CANCELLED
```

Un follow-up puede asignarse a un miembro activo de la misma organización.

`OVERDUE` no se persiste como estado: se deriva a partir de `scheduledAt` cuando un follow-up sigue `PENDING`.

---

## Activities

Las actividades forman el historial de una oportunidad.

Incluyen eventos como:

```text
OPPORTUNITY_CREATED
NOTE
CALL
EMAIL
MEETING
QUOTE_SENT
QUOTE_EXPIRED
STATUS_CHANGE
FOLLOW_UP_COMPLETED
SYSTEM
OTHER
```

Las actividades automáticas se generan desde el backend. El usuario únicamente puede crear manualmente los tipos permitidos.

---

## Priorización de oportunidades

La prioridad se calcula dinámicamente y **no se almacena en base de datos**.

La puntuación considera, entre otros factores:

- follow-ups vencidos o próximos;
- tiempo sin actividad;
- presupuestos enviados pendientes de respuesta;
- estado de la oportunidad;
- ausencia de follow-up.

Resultado:

```text
0–19    LOW
20–44   MEDIUM
45–69   HIGH
70–100  URGENT
```

La API devuelve tanto la puntuación como los motivos que la han generado.

---

## Dashboard

El dashboard diario reúne información comercial relevante, como:

- oportunidades abiertas;
- follow-ups pendientes;
- follow-ups vencidos;
- follow-ups previstos para hoy;
- oportunidades con mayor prioridad.

Endpoint:

```text
GET /api/v1/organizations/{organizationId}/dashboard/today
```

---

## Scheduler

Existe un proceso programado para detectar presupuestos enviados cuya fecha de expiración ya ha pasado.

```text
SENT + expiresAt <= now
        ↓
EXPIRED
        ↓
QUOTE_EXPIRED Activity
```

El proceso se ejecuta periódicamente y utiliza UTC.

---

## API

La API utiliza el prefijo:

```text
/api/v1
```

Principales grupos:

| Módulo | Ruta base |
|---|---|
| Auth | `/api/v1/auth` |
| Organizations | `/api/v1/organizations` |
| Members | `/api/v1/organizations/{organizationId}/members` |
| Customers | `/api/v1/organizations/{organizationId}/customers` |
| Opportunities | `/api/v1/organizations/{organizationId}/opportunities` |
| Quotes | `/api/v1/organizations/{organizationId}/.../quotes` |
| Follow-ups | `/api/v1/organizations/{organizationId}/.../follow-ups` |
| Activities | `/api/v1/organizations/{organizationId}/opportunities/{opportunityId}/activities` |
| Priorities | `/api/v1/organizations/{organizationId}/opportunity-priorities` |
| Dashboard | `/api/v1/organizations/{organizationId}/dashboard/today` |

La documentación interactiva está disponible en:

```text
http://localhost:8080/swagger-ui.html
```

y la especificación OpenAPI en:

```text
http://localhost:8080/v3/api-docs
```

---

## Manejo de errores

La API utiliza un contrato uniforme `ApiError`.

Ejemplo conceptual:

```json
{
  "type": "about:blank",
  "code": "CUSTOMER_NOT_FOUND",
  "title": "Customer not found",
  "status": 404,
  "detail": "...",
  "instance": "/api/v1/...",
  "timestamp": "...",
  "fieldErrors": []
}
```

Se diferencian, entre otros:

```text
400 → validación / petición incorrecta
401 → usuario no autenticado
403 → acceso denegado
404 → recurso no encontrado o recurso de otro tenant
409 → conflicto de negocio o concurrencia
500 → error interno inesperado
```

---

## Ejecución local

### Requisitos

- Java 21
- Docker / Docker Desktop
- Git

El proyecto incluye Maven Wrapper, por lo que no es obligatorio instalar Maven globalmente.

### 1. Levantar PostgreSQL

Desde la raíz del repositorio:

```bash
docker compose up -d
```

La base de datos local utiliza:

```text
database: micropymes
user:     micropymes
password: micropymes
port:     5432
```

Estos valores son exclusivamente de desarrollo local.

### 2. Entrar en el backend

```bash
cd backend
```

### 3. Activar el perfil local

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
```

Linux/macOS:

```bash
export SPRING_PROFILES_ACTIVE=local
```

### 4. Arrancar

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

El backend quedará disponible en:

```text
http://localhost:8080
```

---

## Tests

La suite incluye:

- tests unitarios de dominio;
- tests de servicios con Mockito;
- tests de reglas de prioridad;
- tests de persistencia;
- tests HTTP reales;
- tests de sesión y CSRF;
- test de protección frente a session fixation;
- tests de aislamiento multi-tenant;
- flujo comercial end-to-end;
- PostgreSQL real mediante Testcontainers.

Ejecutar:

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

Los tests de integración levantan automáticamente una instancia temporal de PostgreSQL mediante Testcontainers.

---

## Build

Windows:

```powershell
.\mvnw.cmd clean package
```

Linux/macOS:

```bash
./mvnw clean package
```

El artefacto generado queda en:

```text
backend/target/
```

---

## Configuración

`application.yml` contiene la configuración común.

`application-local.yml` contiene únicamente configuración específica del entorno local, como:

```text
PostgreSQL localhost
show-sql
secure=false para la cookie local
```

El perfil no está hardcodeado en la aplicación.

Se selecciona mediante:

```text
SPRING_PROFILES_ACTIVE
```

Esto permite incorporar posteriormente configuraciones específicas de staging o producción sin modificar el código fuente.

---

## Estado del proyecto

La fase de backend del MVP está completada e incluye:

```text
Arquitectura
Base de datos y migraciones
Autenticación y organizaciones
Clientes
Oportunidades
Presupuestos
Follow-ups
Activities
Priorización
Dashboard
Scheduler
OpenAPI
Seguridad
Tests unitarios
Tests de integración
Tests end-to-end
```

La siguiente etapa del proyecto es el desarrollo del frontend web y su integración con esta API.

---

## Decisiones de alcance del MVP

El backend se ha diseñado deliberadamente sin introducir todavía:

```text
microservicios
aplicación móvil nativa
facturación/suscripciones
IA
integración con WhatsApp
notificaciones externas
password reset
email verification
```

El objetivo actual es mantener un MVP pequeño, coherente y técnicamente sólido antes de ampliar el producto.

---

## Licencia

Pendiente de definir.
