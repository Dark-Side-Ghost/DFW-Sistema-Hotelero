# Sistema de Administración y Reserva de Habitaciones de Hotel (DWF404 · Fase final)

API REST con **Java 21 + Spring Boot 3.5** y frontend en **React (Vite)**. Base de datos **MySQL**.
Seguridad con **JWT**, documentación con **Swagger/OpenAPI**.

```
hotel-proyecto/
├── backend/      API REST (Spring Boot)
├── frontend/     Interfaz (React + Vite)
├── database/     hotel_db.sql  (tablas + datos de ejemplo)
├── postman/      Hotel_API.postman_collection.json
└── README.md     este archivo
```

---

## 1. Cómo ejecutarlo (paso a paso)

### Requisitos
| Herramienta | Versión |
|---|---|
| JDK | **21** (también funciona con 22+, pero compila a 21) |
| Maven | 3.9+ (o el que trae IntelliJ / VS Code) |
| MySQL | 8.0.16+ (los `CHECK` se aplican desde esa versión) |
| Node.js | 18.18+ (recomendado 20 o 22) |

### Paso 1 – Base de datos
1. Abre MySQL Workbench (o la consola) y ejecuta **`database/hotel_db.sql`** completo.
   Crea la base `hotel_db`, las 13 tablas y datos de ejemplo (3 hoteles, 12 habitaciones, 6 servicios…).
   > Ejecútalo **antes** de iniciar la API. Si lo vuelves a ejecutar, borra y recrea todo.

### Paso 2 – Backend
1. Crea tu archivo de configuración local copiando el ejemplo:
   ```bash
   cd backend
   cp .env.example .env        # en Windows: copy .env.example .env
   ```
   Abre `backend/.env` y escribe tus valores (el archivo **no se sube a GitHub**):
   ```properties
   DB_USER=root
   DB_PASSWORD=tu_clave_de_mysql
   JWT_SECRET=una-clave-aleatoria-de-64-caracteres-o-mas
   ```
   Genera la clave JWT con uno de estos comandos:
   ```bash
   openssl rand -base64 64                                   # Linux / macOS / Git Bash
   # PowerShell (Windows)
   [Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
   ```
   Alternativa: definir las mismas variables de entorno del sistema (`DB_USER`, `DB_PASSWORD`, `JWT_SECRET`) o en la configuración de ejecución del IDE.
   Si no existe `.env`, la API usa valores de desarrollo (`root`/`root` y una clave pública de ejemplo): sirve para probar, **no** para un sistema real.
2. Desde la carpeta `backend/` (donde esta el `.env`):
   ```bash
   mvn spring-boot:run
   ```
   (o abre la carpeta en IntelliJ y ejecuta `HotelApplication`; activa *Annotation Processing* si IntelliJ lo pide, por Lombok).
3. Al iniciar, la API crea **3 usuarios de prueba** (ver sección 2). API en `http://localhost:8080`.
4. Swagger: **http://localhost:8080/swagger-ui.html** → botón *Authorize* → pega el token que devuelve `POST /api/auth/login`.

### Paso 3 – Frontend
```bash
cd frontend
npm install
npm run dev
```
Abre **http://localhost:5173**. El frontend llama a `/api/...` y Vite lo redirige a `localhost:8080` (proxy en `vite.config.js`), por eso no hay problemas de CORS.

### Paso 4 – Postman
Importa `postman/Hotel_API.postman_collection.json`. Ejecuta primero la carpeta **0. Login** (guarda los tokens en variables) y luego el resto en orden, o usa el *Runner* con toda la colección. Cada request tiene tests que verifican el código HTTP esperado (incluye casos de error: 400, 401, 403, 409).

---

## 2. Usuarios de prueba

| Rol | Correo | Contraseña | Qué puede hacer |
|---|---|---|---|
| ADMIN | admin@hotel.com | Admin123* | Todo: hoteles, habitaciones, tipos, servicios, políticas, usuarios, estadísticas, reservas, reclamos |
| RECEPCIONISTA | recepcion@hotel.com | Recepcion123* | Reservas del día, registrar pagos, check-in/out, cancelar, reclamos, crear reservas |
| CLIENTE | cliente@hotel.com | Cliente123* | Buscar, reservar, pagar, modificar/cancelar sus reservas, reportar problemas |

Cualquier persona puede registrarse desde la pantalla de login (se crea siempre como CLIENTE).

---

## 3. Cómo funciona (flujo)

### Flujo del cliente
1. **Login / Registro** → el backend devuelve un **token JWT** + el rol. El frontend lo guarda y lo manda en cada petición (`Authorization: Bearer ...`).
2. **Buscar** (ciudad, fechas, huéspedes, tipo opcional) → `GET /api/habitaciones/disponibles`. El backend excluye habitaciones en mantenimiento, sin capacidad suficiente o con una reserva activa que se cruce en fechas.
3. **Elegir habitación** → el frontend calcula noches × tarifa.
4. **Datos del huésped + servicios adicionales** (desayuno, transporte, lavandería…).
5. **Resumen** → al confirmar, `POST /api/reservas` crea la reserva en estado **PENDIENTE** y calcula el total en el servidor (el precio **nunca** lo manda el frontend).
6. **Pago (simulado)** → `POST /api/reservas/{id}/pagos`:
   - Tarjeta o transferencia → pago `COMPLETADO` y reserva **CONFIRMADA**.
   - Efectivo siendo cliente → pago `PENDIENTE`; la reserva sigue PENDIENTE hasta que recepción registre el cobro.
7. **Mis reservas** → pagar el saldo, modificar fechas, cancelar (con cargo/reembolso según la política del hotel) o reportar un problema (reclamo).

### Flujo de recepción
`PENDIENTE` → (registrar pago) → `CONFIRMADA` → (check-in, desde la fecha de entrada) → `EN_CURSO` → (check-out) → `FINALIZADA`.
Cualquier reserva PENDIENTE/CONFIRMADA puede pasar a `CANCELADA`.
Check-in marca la habitación `OCUPADA`; check-out la deja `DISPONIBLE`.
Los reclamos se gestionan con estados `PENDIENTE → EN_PROCESO → RESUELTO / RECHAZADO`.

### Flujo del administrador
CRUD de hoteles, habitaciones (con servicios incluidos), tipos, servicios, políticas y usuarios (crear, editar, eliminar, **activar/desactivar**), y pantalla de **estadísticas** (reservas del mes, ingresos, ocupación por hotel, cancelaciones, ingresos por mes).

### Reglas de negocio importantes
- **Cancelación:** si faltan `dias_previos_limite` días o más → sin cargo (reembolso = lo pagado). Si faltan menos → cargo = `porcentaje_cargo`% del total; reembolso = pagado − cargo (mínimo 0). Los pagos reembolsados pasan a `REEMBOLSADO`.
- **Disponibilidad:** dos reservas activas (`PENDIENTE`, `CONFIRMADA`, `EN_CURSO`) no pueden cruzarse en la misma habitación. Salida y entrada el mismo día **sí** se permiten.
- **Estadía máxima:** 30 noches. **Huésped titular:** mayor de 18 años.
- **Pago:** es simulado (como definió la Fase 1). Los datos de tarjeta se validan pero **no se guardan**.

---

## 4. Qué hace cada parte y por qué se hizo así

### Backend (`backend/src/main/java/com/udb/hotel`)
| Paquete | Contenido | Por qué |
|---|---|---|
| `controller` | Endpoints REST (reciben JSON, llaman al servicio) | Capa delgada: solo HTTP y permisos (`@PreAuthorize`) |
| `service` | Lógica de negocio (reservas, pagos, cancelación, estadísticas…) | Toda regla vive aquí, así es fácil de explicar y probar |
| `repository` | Interfaces Spring Data JPA + 3 consultas JPQL | JPA evita escribir SQL para el CRUD |
| `entity` | Las 12 entidades JPA (+ tabla intermedia `habitacion_servicio`) = las 13 tablas | Mapeo 1:1 con el diagrama de la Fase 1 |
| `dto` | Cuerpos de petición/respuesta (Java `record`) con validaciones | No exponemos entidades con relaciones ni el hash de contraseña |
| `security` | JWT: `JwtService`, `JwtAuthFilter`, manejo 401/403 en JSON | Autenticación *stateless* (sin sesiones) |
| `config` | `SecurityConfig`, `OpenApiConfig` (Swagger), `DataInitializer` | Configuración en un solo lugar |
| `exception` | `ApiException`, `GlobalExceptionHandler`, `ApiError` | Todos los errores salen con el mismo formato y mensajes amigables |
| `util` | `Fechas` (zona El Salvador), `Reglas` (regex), `SecurityUtil` | Evita repetir código |

Decisiones de simplicidad:
- **Entidades simples** (`Hotel`, `TipoHabitacion`, `Servicio`) se usan directamente como cuerpo de la petición con sus validaciones; las que tienen relaciones usan DTO.
- **Relaciones `EAGER`** y `open-in-view=false`: no hay errores de carga perezosa al convertir a JSON.
- **Estados como `String`** (igual que el diccionario de datos), validados con `@Pattern`.
- **`DataInitializer`** crea roles y usuarios con BCrypt al iniciar, para no calcular hashes a mano.
- **Seguridad:** todo endpoint exige token salvo `/api/auth/**` y Swagger. El rol se revisa con `@PreAuthorize` (`ADMIN`, `RECEPCIONISTA`, `CLIENTE`). Un cliente solo ve y modifica **sus** reservas (se comprueba en `ReservaService.obtenerEntidad`).
- **JWT (jjwt 0.12.6):** la clave sale de `JWT_SECRET` (variable de entorno), expira en 8 h.

### Frontend (`frontend/src`)
| Archivo | Para qué |
|---|---|
| `api.js` | Cliente axios: agrega el token, redirige al login si expira, extrae mensajes de error |
| `auth.jsx` | Contexto de sesión (login, registro, logout) |
| `validators.js` | Expresiones regulares y funciones de validación (mismas reglas que el backend) |
| `components/CrudPage.jsx` | **Una sola pantalla CRUD reutilizable** (listar/crear/editar/eliminar) para Hoteles, Habitaciones, Tipos, Servicios, Políticas y Usuarios |
| `components/PagoForm.jsx` | Formulario de pago (usado por el cliente y por recepción) |
| `pages/Reservar.jsx` | Flujo de 6 pasos (buscar → … → confirmación) |
| `pages/MisReservas.jsx`, `ReservasStaff.jsx`, `Reclamos.jsx`, `Estadisticas.jsx` | Pantallas por rol |
| `App.jsx` | Rutas protegidas por sesión y por rol |

Por qué así: con `CrudPage` seis pantallas administrativas se definen con ~30 líneas cada una (solo campos y columnas), lo que reduce errores y facilita la defensa.

---

## 5. Validaciones (servidor **y** frontend)

| Tipo (documento) | Ejemplos implementados |
|---|---|
| Campos vacíos | `@NotBlank`/`@NotNull` en el backend; el frontend marca los campos requeridos en rojo |
| Tipo de dato (regex) | Nombres solo letras; teléfono `^\+?[0-9]{8,15}$`; DUI `^\d{8}-\d$`; NIT; correo; contraseña (8+, letra y número); capacidad y estrellas enteros; precios decimales con 2 decimales |
| Congruencia con la realidad | Fecha de entrada ≥ hoy; salida > entrada; huésped ≥ 18 años; huéspedes ≤ capacidad de la habitación; habitación no reservada en esas fechas; tarjeta no vencida; no pagar una reserva ya pagada |

El **servidor siempre valida** (es lo que pide el documento); el frontend valida además para dar respuesta inmediata.

### Formato de errores (mensajes amigables)
```json
{
  "timestamp": "2026-10-28T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Hay datos inválidos o incompletos. Revisa los campos indicados.",
  "ruta": "/api/hoteles",
  "errores": { "nombre": "El nombre del hotel es obligatorio" }
}
```
Códigos usados: 200, 201, 204, 400 (datos inválidos), 401 (sin sesión / credenciales), 403 (sin permiso), 404, 409 (duplicado o conflicto), 500.

---

## 6. Endpoints principales (todos en Swagger)

| Módulo | Endpoints | Quién |
|---|---|---|
| Auth | `POST /api/auth/login`, `POST /api/auth/registro` | Público |
| Hoteles / Tipos / Servicios / Políticas | `GET` lista y por id; `POST`, `PUT /{id}`, `DELETE /{id}` | GET: autenticado · escritura: ADMIN |
| Habitaciones | CRUD en `/api/habitaciones`, `GET /api/habitaciones/disponibles?ciudad&entrada&salida&huespedes&tipoId` | GET: autenticado · escritura: ADMIN |
| Reservas | `POST /api/reservas`, `GET /mias`, `GET /{id}`, `PUT /{id}`, `POST /{id}/pagos`, `POST /{id}/cancelar` | Autenticado (dueño o staff) |
| Reservas (staff) | `GET /api/reservas`, `GET /hoy`, `POST /{id}/check-in`, `POST /{id}/check-out` | ADMIN, RECEPCIONISTA |
| Reclamos | `POST /api/reclamos`, `GET /mios` · `GET /api/reclamos`, `PUT /{id}/estado` · `DELETE /{id}` | Cliente · Staff · ADMIN |
| Usuarios | CRUD + `PATCH /api/usuarios/{id}/estado` (activar/desactivar) | ADMIN |
| Estadísticas | `GET /api/estadisticas` | ADMIN |

---

## 7. Cambios respecto al modelo de la Fase 1
El documento permite actualizar la base de datos. Se **agregaron columnas** (las mismas 13 tablas):
- `hoteles`: `direccion`, `telefono` (formulario "Agregar hotel" del prototipo).
- `pagos`: `fecha_pago` (la Fase 1 dice que el pago registra fecha).
- `reservas`: `fecha_creacion` (estadísticas del mes) y nuevo estado `EN_CURSO` (check-in realizado, botón "Registrar check-in" del prototipo).
- `reclamos`: `fecha_creacion`.

---

## 8. Correspondencia con la rúbrica

| Criterio | Dónde está |
|---|---|
| API funcional al 100 % | Todos los módulos de la Fase 1 + frontend completo |
| Estructura de proyecto | Paquetes `controller/service/repository/entity/dto/security/config/exception/util` |
| Almacenamiento y lectura | JPA + MySQL, CRUD de todos los módulos, JSON |
| Autenticación y login | `POST /api/auth/login` devuelve token y rol; roles ADMIN/RECEPCIONISTA/CLIENTE |
| Sesiones y estados por usuario | Spring Security + JWT en **todos** los endpoints; activar/desactivar usuarios |
| Validaciones del servidor | Bean Validation + reglas en servicios (sección 5) |
| Frontend funcional | React: login, registro, admin, recepción y flujo del cliente |
| Errores amigables | `GlobalExceptionHandler` + alertas en el frontend |
| Documentación API | Swagger UI `/swagger-ui.html`, OpenAPI en `/v3/api-docs` |
| Documento / manual técnico | Este README + diagrama de la Fase 1 (ver sección 9) |

---

## 9. Entrega y defensa

- **Aula Digital (límite 29/10/2026 23:59):** según el consenso del documento, se entrega **un PDF con la portada y el enlace de GitHub** (Calibri 10, carta, márgenes 2 cm, interlineado 1.15, justificado). Incluye la URL del repo, el usuario de GitHub y los usuarios de prueba (sección 2).
- **No hagas commits después de entregar:** el documento anula el trabajo si se modifica el código luego de la fecha.
- Sube también `database/hotel_db.sql` y `postman/` al repositorio.
- **Defensa (30/10):** todos deben poder explicar: cómo viaja el JWT (login → filtro → `SecurityContext`), por qué el total se calcula en el servidor, cómo se detecta el cruce de fechas (`contarSolapadas` y `buscarDisponibles`), cómo se calcula el reembolso, qué hace `GlobalExceptionHandler`, y para qué sirve cada paquete.

---

## 10. Dependencias y versiones (para el manual técnico)
**Backend:** Spring Boot 3.5.6 · Java 21 · spring-boot-starter-web / data-jpa / validation / security · mysql-connector-j (gestionado por Boot) · springdoc-openapi-starter-webmvc-ui 2.8.13 · jjwt 0.12.6 · Lombok (gestionado por Boot).
**Frontend:** React 18.3 · react-router-dom 6.28 · axios 1.7 · Vite 5.4 · @vitejs/plugin-react 4.3.

Notas por cambios recientes en dependencias:
- Se usa **Spring Boot 3.5.x** (línea estable). Spring Boot 4 cambió módulos, Jackson y Security; no se mezcla con `springdoc 2.8.x`/`jjwt 0.12.x`. Si tu cátedra exige Boot 4, hay que subir `springdoc` a la serie 3.x y revisar imports.
- **jjwt 0.12.x** usa la API nueva (`Jwts.builder().subject(...)`, `Jwts.parser().verifyWith(...)`); los ejemplos antiguos con `setSubject`/`parserBuilder` ya no sirven.
- **Spring Security 6.x:** configuración con `SecurityFilterChain` y lambdas (no existe `WebSecurityConfigurerAdapter`).
- **Lombok** se declara en `annotationProcessorPaths` del `pom.xml` porque desde JDK 23 los procesadores de anotaciones no se activan solos.
- **springdoc 2.8** genera OpenAPI 3.1 por defecto; funciona sin configuración adicional.

---

## 11. Problemas comunes
| Síntoma | Solución |
|---|---|
| `Access denied for user 'root'` | Define `DB_PASSWORD` con tu clave de MySQL |
| `Table ... doesn't exist` / datos vacíos | Ejecuta `database/hotel_db.sql` (la API puede crear tablas vacías sin los datos de ejemplo) |
| `cannot find symbol ... getXxx()` | Falta Lombok: activa *Annotation Processing* en el IDE y recarga Maven |
| Login devuelve 401 con la clave correcta | Revisa mayúsculas: `Admin123*`; verifica que la API haya creado los usuarios (log "Usuario de prueba creado") |
| El frontend dice "No se pudo conectar" | Verifica que la API corra en el puerto 8080 y que usaste `npm run dev` (proxy) |
| 403 al usar una pantalla | El rol no tiene permiso: inicia sesión con el usuario adecuado |
| Swagger sin permisos | Pulsa *Authorize* y pega **solo el token** (sin la palabra Bearer) |

---

## 12. Subir el proyecto a GitHub

Repositorio del equipo: `https://github.com/Helen-portillo/DFW-Sistema-Hotelero.git`

1. **Antes de subir**: confirma que existen `.gitignore` (raíz) y `backend/.env.example`, y que **no** vas a subir `backend/.env`, `backend/target/` ni `frontend/node_modules/` (ya están ignorados). Revisa que ninguna clave real aparezca en el código:
   ```bash
   git grep -n -i "password\|secret" -- "*.properties"
   ```
2. Desde la carpeta raíz del proyecto (`hotel-proyecto/`, la que contiene `backend/` y `frontend/`):
   ```bash
   git init -b main
   git config user.name  "Tu Nombre"
   git config user.email "tu_correo@ejemplo.com"
   git add .
   git status                       # verifica que NO aparezcan .env, target/ ni node_modules/
   git commit -m "Fase final: API REST Spring Boot + frontend React"
   git remote add origin https://github.com/Helen-portillo/DFW-Sistema-Hotelero.git
   git push -u origin main
   ```
3. Si el repositorio ya tiene contenido (por ejemplo la Fase 1) y `git push` lo rechaza, primero integra lo que hay:
   ```bash
   git pull origin main --allow-unrelated-histories
   git push -u origin main
   ```
4. **Autenticación:** GitHub no acepta la contraseña de la cuenta. Usa un *Personal Access Token* (Settings → Developer settings → Personal access tokens, permiso `repo`) como contraseña, o inicia sesión con *GitHub CLI* (`gh auth login`) o con el administrador de credenciales de Git. Los demás integrantes deben tener permiso de colaborador en el repositorio.
5. Verifica en la página del repositorio que están `backend/`, `frontend/`, `database/`, `postman/` y este `README.md`, y que **no** aparece `.env`.
6. Si subiste un secreto por error: cámbialo de inmediato (genera otro `JWT_SECRET` y cambia la contraseña de MySQL); borrar el archivo en un commit nuevo no lo elimina del historial.
7. **Fecha límite:** el documento anula los trabajos con commits posteriores a la entrega (29/10/2026). Haz el último push antes de entregar el PDF y no vuelvas a subir cambios.

### Qué hace falta en la defensa (no se sube al repositorio)
Cada integrante debe tener a mano el código, la base de datos (`database/hotel_db.sql`), el documento de la Fase 1 y el manual técnico. Para ejecutar el proyecto recién clonado: `git clone ...`, crear `backend/.env` (paso 2 de la sección 1), ejecutar el script SQL, `mvn spring-boot:run` y `npm install && npm run dev`.
