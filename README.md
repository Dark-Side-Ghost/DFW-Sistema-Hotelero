# Sistema de Administración y Reserva de Habitaciones de Hotel

**Proyecto de cátedra · DWF404 G02-T · Universidad Don Bosco · Fase Final**
Docente: Ing. Mario Alvarado

API REST con **Java 21 + Spring Boot 3.5** y frontend en **React 18 (Vite)**, con base de datos **MySQL**, autenticación **JWT** y documentación **Swagger/OpenAPI**.

Repositorio: https://github.com/Dark-Side-Ghost/DFW-Sistema-Hotelero

---

## Integrantes del equipo

| Nombre | Carnet | Rol en el proyecto |
|---|---|---|
| William Aaron Peralta Cruz | PC210574 | Líder de proyecto / Desarrollador Backend |
| Delmy Beatriz González Aragón | GA252427 | Desarrolladora Frontend |
| Helen Verónica Portillo Rodríguez | PR230742 | Analista de requerimientos / Desarrolladora Backend |
| Roberto Antonio Cabrera Navas | CN233178 | Administrador de base de datos |

> Usuario de GitHub del repositorio: `Dark-Side-Ghost`

---

## Contenido del repositorio

```
├── backend/     API REST (Spring Boot, Maven)
├── frontend/    Interfaz web (React + Vite)
├── database/    hotel_db.sql  (13 tablas + datos de ejemplo)
├── postman/     Hotel_API.postman_collection.json  (74 pruebas)
└── README.md
```

---

## Cómo ejecutar el proyecto (guía para el evaluador)

### Requisitos
JDK **21** · Maven 3.9+ · MySQL **8.0.16+** · Node.js **18.18+** (recomendado 20 o 22)

### 1. Base de datos
Ejecutar **`database/hotel_db.sql`** completo en MySQL (Workbench o consola). Crea la base `hotel_db`, las 13 tablas y datos de ejemplo (3 hoteles, 12 habitaciones, 6 servicios, políticas de cancelación). Debe ejecutarse **antes** de iniciar la API.

### 2. Configuración local: archivo `.env`
El repositorio **no contiene contraseñas reales**. Las credenciales de MySQL y la clave de firma de los tokens JWT se leen de un archivo `backend/.env` que cada persona crea en su equipo y que **no se sube a GitHub** (está en `.gitignore`).

1. Dentro de `backend/`, copiar la plantilla:
   ```bash
   cp .env.example .env          # Windows: copy .env.example .env
   ```
2. Editar `backend/.env` y poner los valores propios, **cada variable en una sola línea, sin espacios ni comillas**:
   ```properties
   DB_USER=root
   DB_PASSWORD=la_clave_de_su_mysql
   JWT_SECRET=una_clave_aleatoria_de_40_caracteres_o_mas
   ```

| Variable | Para qué sirve | Si no se define |
|---|---|---|
| `DB_USER` | Usuario de MySQL | Usa `root` |
| `DB_PASSWORD` | Contraseña de MySQL | Usa `root` |
| `JWT_SECRET` | Clave con la que se firman los tokens JWT (mínimo 32 caracteres) | Usa una clave de desarrollo incluida en `application.properties` (pública, solo para pruebas) |

Notas:
- Si la contraseña de MySQL del evaluador **es `root`**, no hace falta crear el `.env`; el proyecto arranca con los valores por defecto.
- El `.env` se lee desde la carpeta desde la que se inicia la API, por eso se recomienda ejecutar `mvn spring-boot:run` **dentro de `backend/`**. En un IDE, configurar ese directorio como *working directory*.
- Alternativa equivalente: definir `DB_USER`, `DB_PASSWORD` y `JWT_SECRET` como variables de entorno del sistema.
- Para generar una clave JWT: `openssl rand -base64 64` (Linux/macOS/Git Bash) o, en PowerShell:
  `[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])`

### 3. Backend
```bash
cd backend
mvn spring-boot:run
```
La API queda en **http://localhost:8080**. Al iniciar crea los roles y los usuarios de prueba (ver más abajo).
Documentación interactiva: **http://localhost:8080/swagger-ui.html** (botón *Authorize*: pegar solo el token devuelto por `POST /api/auth/login`).

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```
Abrir **http://localhost:5173**. Vite redirige las llamadas `/api/...` al backend (proxy en `vite.config.js`), por lo que no hay problemas de CORS.

### 5. Pruebas con Postman
Importar `postman/Hotel_API.postman_collection.json`, ejecutar primero la carpeta **0. Login** (guarda los tokens de los tres roles) y luego el resto en orden o con el *Runner*. Cada request verifica el código HTTP esperado, incluyendo casos de error (400, 401, 403, 409).

---

## Usuarios predefinidos para pruebas

| Rol | Correo | Contraseña | Qué puede hacer |
|---|---|---|---|
| ADMIN | admin@hotel.com | Admin123* | Todo: hoteles, habitaciones, tipos, servicios, políticas, usuarios (crear/editar/eliminar/activar/desactivar), estadísticas, reservas y reclamos |
| RECEPCIONISTA | recepcion@hotel.com | Recepcion123* | Reservas del día, registrar pagos, check-in/check-out, cancelar, gestionar reclamos, crear reservas |
| CLIENTE | cliente@hotel.com | Cliente123* | Buscar y reservar, pagar, modificar o cancelar sus reservas, reportar problemas |

Cualquier persona también puede registrarse desde la pantalla de login (se crea siempre como CLIENTE).

---

## Recorrido sugerido para evaluar

1. **Login** con cada rol y verificar que el menú cambia según el rol.
2. **Cliente:** *Buscar y reservar* → elegir habitación → datos del huésped y servicios → resumen → pago (tarjeta de prueba `4111 1111 1111 1111`, vence `12/30`, CVV `123`) → código de confirmación. Luego *Mis reservas*: modificar fechas, cancelar (ver reembolso) y reportar un problema.
3. **Recepción:** *Reservas del día* → registrar pago, check-in y check-out; *Reclamos* → cambiar estado.
4. **Administrador:** CRUD de hoteles, habitaciones, tipos, servicios, políticas y usuarios (activar/desactivar); *Estadísticas*.
5. **Validaciones:** probar fecha de entrada pasada, correo inválido, campos vacíos, DUI mal formado, tarjeta vencida, reserva de una habitación ya ocupada, y acceder con un rol sin permiso. Todos devuelven mensajes claros.
6. **Swagger y Postman:** consultar la documentación y ejecutar la colección.

---

## Funcionamiento general

**Flujo de la reserva:** buscar (ciudad, fechas, huéspedes) → elegir habitación → datos del huésped + servicios adicionales → resumen → pago → confirmación.

**Estados de una reserva:** `PENDIENTE` → (pago) → `CONFIRMADA` → (check-in) → `EN_CURSO` → (check-out) → `FINALIZADA`. Una reserva PENDIENTE o CONFIRMADA puede pasar a `CANCELADA`.

**Reglas de negocio:**
- **Disponibilidad:** una habitación no puede tener dos reservas activas que se crucen en fechas; no se reservan habitaciones en mantenimiento ni por encima de su capacidad.
- **Precio:** lo calcula siempre el servidor (noches × tarifa + servicios adicionales).
- **Cancelación:** si faltan al menos los días previos definidos por la política del hotel no hay cargo; si no, se cobra el porcentaje indicado. Reembolso = pagado − cargo (nunca negativo).
- **Pago:** simulado, como definió el alcance de la Fase 1. Los datos de tarjeta se validan pero no se guardan. Efectivo realizado por un cliente queda pendiente hasta que recepción lo cobra.
- **Estadía máxima:** 30 noches. **Huésped titular:** mayor de 18 años.

### Arquitectura del backend (`backend/src/main/java/com/udb/hotel`)

| Paquete | Contenido |
|---|---|
| `controller` | Endpoints REST; validan el cuerpo (`@Valid`) y el rol (`@PreAuthorize`) |
| `service` | Lógica de negocio (reservas, pagos, cancelaciones, estadísticas…) |
| `repository` | Spring Data JPA y consultas JPQL |
| `entity` | Entidades JPA (mapeo de las 13 tablas) |
| `dto` | Objetos de petición/respuesta con validaciones |
| `security` | JWT: emisión, filtro de autenticación y respuestas 401/403 |
| `config` | Spring Security, CORS, Swagger y creación de roles/usuarios iniciales |
| `exception` | Manejo global de errores con formato y mensajes amigables |
| `util` | Fecha de El Salvador, expresiones regulares, usuario autenticado |

### Frontend (`frontend/src`)
`api.js` (axios + token), `auth.jsx` (sesión), `validators.js` (validaciones), `App.jsx` (rutas protegidas por rol), `components/CrudPage.jsx` (pantalla CRUD reutilizable para hoteles, habitaciones, tipos, servicios, políticas y usuarios), `components/PagoForm.jsx` y `pages/` (13 pantallas).

---

## Seguridad, validaciones y errores

- **Autenticación:** `POST /api/auth/login` devuelve un token JWT (vigencia 8 horas) y el rol del usuario. Todos los endpoints exigen token, salvo login, registro y Swagger.
- **Autorización por rol:** ADMIN, RECEPCIONISTA y CLIENTE. Un cliente solo puede ver y modificar sus propias reservas. Un usuario desactivado no puede iniciar sesión ni usar tokens anteriores.
- **Validaciones en el servidor** (el frontend las repite para dar respuesta inmediata):
  - *Campos vacíos:* `@NotBlank`, `@NotNull`.
  - *Tipo de dato (regex):* nombres solo letras, teléfono, DUI `^\d{8}-\d$`, correo, contraseña (8+ con letra y número), enteros y decimales.
  - *Congruencia con la realidad:* fecha de entrada ≥ hoy, salida > entrada, mayoría de edad, huéspedes ≤ capacidad, sin cruce de reservas, tarjeta no vencida.
- **Errores amigables:** todas las respuestas de error tienen el mismo formato:
  ```json
  { "timestamp": "...", "status": 400, "error": "Bad Request",
    "mensaje": "Hay datos inválidos o incompletos. Revisa los campos indicados.",
    "ruta": "/api/hoteles", "errores": { "nombre": "El nombre del hotel es obligatorio" } }
  ```
  Códigos: 200, 201, 204, 400, 401 (sin sesión), 403 (sin permiso), 404, 409 (duplicado/conflicto), 500.

---

## Endpoints principales

| Módulo | Endpoints | Acceso |
|---|---|---|
| Auth | `POST /api/auth/login`, `POST /api/auth/registro` | Público |
| Hoteles, tipos, servicios, políticas | `GET` (lista/id) · `POST`, `PUT /{id}`, `DELETE /{id}` bajo `/api/hoteles`, `/api/tipos-habitacion`, `/api/servicios`, `/api/politicas` | Lectura: autenticado · Escritura: ADMIN |
| Habitaciones | CRUD en `/api/habitaciones` y `GET /api/habitaciones/disponibles?ciudad&entrada&salida&huespedes&tipoId` | Lectura/búsqueda: autenticado · Escritura: ADMIN |
| Reservas | `POST /api/reservas`, `GET /mias`, `GET /{id}`, `PUT /{id}`, `POST /{id}/pagos`, `POST /{id}/cancelar` | Autenticado (dueño o personal) |
| Reservas (personal) | `GET /api/reservas`, `GET /hoy`, `POST /{id}/check-in`, `POST /{id}/check-out` | ADMIN, RECEPCIONISTA |
| Reclamos | `POST /api/reclamos`, `GET /mios` · `GET /api/reclamos`, `PUT /{id}/estado` · `DELETE /{id}` | Cliente · Personal · ADMIN |
| Usuarios | CRUD + `PATCH /api/usuarios/{id}/estado` | ADMIN |
| Estadísticas | `GET /api/estadisticas` | ADMIN |

---

## Modelo de datos

13 tablas: `roles`, `usuarios`, `huespedes`, `hoteles`, `tipos_habitacion`, `habitaciones`, `politicas_cancelacion`, `servicios`, `habitacion_servicio`, `reservas`, `reserva_servicios`, `pagos`, `reclamos` (modelo de la Fase 1).

Actualizaciones permitidas por el documento de la Fase Final:
- `hoteles`: columnas `direccion` y `telefono`.
- `pagos`: columna `fecha_pago`.
- `reservas`: columna `fecha_creacion` y nuevo estado `EN_CURSO` (check-in realizado).
- `reclamos`: columna `fecha_creacion`.

---

## Correspondencia con la rúbrica

| Criterio | Dónde se cumple |
|---|---|
| Aplicación funcional al 100 % | Todos los módulos de la Fase 1 operativos de punta a punta (API + frontend) |
| Estructura de proyecto | Paquetes `controller / service / repository / entity / dto / security / config / exception / util` |
| Almacenamiento y lectura de datos | JPA + MySQL; CRUD de todos los módulos; JSON |
| Autenticación y login | `POST /api/auth/login` devuelve token y rol; perfil administrador con gestión de usuarios |
| Sesiones y estados por usuario | Spring Security + JWT en todos los endpoints; activar/desactivar usuarios |
| Validaciones del lado del servidor | Bean Validation y reglas en los servicios (vacíos, formato, congruencia) |
| Frontend funcional | React: login, registro, administración, recepción y flujo completo del cliente |
| Errores y mensajes amigables | `GlobalExceptionHandler` y alertas en el frontend |
| Documentación de la API | Swagger UI en `/swagger-ui.html` (OpenAPI en `/v3/api-docs`) |
| Documento Fase 1 y manual técnico | Documento de entrega con portada y enlace de GitHub; este README amplía la información técnica |

---

## Dependencias y versiones

**Backend:** Java 21 · Spring Boot 3.5.6 (web, data-jpa, validation, security) · mysql-connector-j · springdoc-openapi-starter-webmvc-ui 2.8.13 · jjwt 0.12.6 · Lombok.
**Frontend:** React 18.3 · react-router-dom 6.28 · axios 1.7 · Vite 5.4 · @vitejs/plugin-react 4.3.

---

## Problemas comunes al ejecutar

| Síntoma | Solución |
|---|---|
| `Access denied for user 'root'` | La contraseña de MySQL no coincide: revisar `DB_PASSWORD` en `backend/.env` |
| `JWT_SECRET debe tener al menos 32 caracteres` | Revisar que `JWT_SECRET=` y su valor estén en la misma línea y sin espacios |
| Tablas inexistentes o sin datos | Ejecutar `database/hotel_db.sql` antes de iniciar la API |
| `cannot find symbol ... getXxx()` al compilar en un IDE | Activar *Annotation Processing* (Lombok) y recargar Maven |
| El frontend dice “No se pudo conectar con el servidor” | Verificar que la API esté en el puerto 8080 y que el frontend se inició con `npm run dev` |
| Login rechazado con la clave correcta | Distinguir mayúsculas/minúsculas (`Admin123*`) y revisar en el log de la API el mensaje “Usuario de prueba creado” |

