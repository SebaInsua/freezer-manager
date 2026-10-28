# Freezer Manager --- Roadmap y backlog

> Documento de continuidad del proyecto. Actualizarlo al cerrar cada
> tarea y subir los cambios a GitHub.
>
> **Última actualización:** 28/10/2026, después del merge de FM-041.
>
> **Objetivo actual:** preparar una primera versión funcional y
> presentable para portfolio, demostrando desarrollo de una API REST con
> Spring Boot, persistencia, reglas de negocio, autenticación y
> autorización JWT, pruebas automatizadas y ejecución con Docker.

## 1. Contexto del proyecto

**Freezer Manager** es una API REST para administrar alimentos,
almacenamientos e inventario de un freezer, registrar ingresos y egresos
y consultar movimientos.

### Stack confirmado

-   Java 21
-   Spring Boot 4
-   Maven
-   Spring Data JPA / Hibernate
-   PostgreSQL para desarrollo
-   H2 y perfil `test` para pruebas
-   JUnit y Mockito; pruebas de integración con Spring Boot / MockMvc
-   OpenAPI/Swagger para documentar y explorar la API

### Arquitectura actual

-   `domain`: entidades y enums de alimentos e inventario.
-   `dto`: requests y respuestas de la API.
-   `repository`: persistencia con Spring Data JPA.
-   `service`: lógica de negocio.
-   `controller`: endpoints REST.
-   `exception`: excepciones y manejo global de errores.

## 2. Estado actual --- FM-035 a FM-041 completadas

**Todas las tareas FM-035, FM-036, FM-037, FM-038, FM-039, FM-040 y
FM-041 se consideran completadas e integradas en `main`, según la
confirmación del usuario.** No volver a planificarlas como pendientes.

-   [x] **FM-035 --- Documentar y verificar el contrato actual de la
    API.** Documentación OpenAPI/Swagger y revisión del contrato.
-   [x] **FM-036 --- Revisión de reglas de negocio e inventario.**
    Reglas y validaciones de inventario implementadas/revisadas.
-   [x] **FM-037 --- Consistencia y concurrencia del inventario.**
    Marcada como completada según el estado confirmado del backlog.
-   [x] **FM-038 --- Paginación, orden y filtros de listados.** Marcada
    como completada según el estado confirmado del backlog.
-   [x] **FM-039 --- Inventario por lotes.** Marcada como completada
    según el estado confirmado del backlog.
-   [x] **FM-040 --- Fechas de vencimiento y alertas.** Marcada como
    completada según el estado confirmado del backlog.
-   [x] **FM-041 --- Capacidad del freezer y espacio disponible.**
    Cálculo de capacidad estimada y espacio recomendado, con factor
    aproximado de 3.5 litros/kg y límite recomendado del 80%.

### Hito más reciente de Git

-   Commit de FM-041: `e44f70f` ---
    `feat: add storage capacity estimation`
-   Rama `feat/storage-capacity` integrada en `main` y eliminada después
    del merge.
-   El usuario confirmó que el repositorio quedó limpio después del
    merge.
-   Última suite reportada tras FM-041: **135 tests, 0 failures, 0
    errors, 0 skipped**.
-   No asumir que ese recuento sigue vigente después de futuras
    modificaciones; ejecutar la suite al cerrar cada tarea.

## 3. Objetivo de la primera versión presentable (MVP)

El objetivo es que una persona técnica pueda clonar y ejecutar el
proyecto, registrarse, autenticarse, operar el inventario y consultar
movimientos. La API debe demostrar buenas prácticas de backend, no solo
operaciones CRUD.

### Incluido en el MVP

-   Registro de usuario e inicio de sesión.
-   Contraseñas almacenadas con hash seguro.
-   Autenticación mediante JWT.
-   Protección de endpoints con Spring Security.
-   Autorización y aislamiento de los datos por usuario.
-   Historial y consultas de ingresos/egresos con filtros útiles.
-   PostgreSQL y ejecución reproducible mediante Docker Compose.
-   Configuración mediante variables de entorno; secretos fuera del
    repositorio.
-   Pruebas unitarias y de integración.
-   README y documentación OpenAPI/Swagger con ejemplos de autenticación
    y uso.

### Fuera del alcance inicial

Evitar ampliar el alcance sin una necesidad concreta. El objetivo es
terminar una primera versión sólida y demostrable.

## 4. Backlog pendiente

Las tareas nuevas comienzan en FM-045 para conservar los identificadores
históricos. Confirmar el alcance contra el código actual antes de
implementar cada tarea.

### FM-045 --- Registro de usuarios, autenticación JWT y autorización

**Prioridad:** muy alta · **Tipo:** seguridad/backend · **Estado:** en progreso

La implementación principal se encuentra realizada en la rama `feat/user-authentication`. Incluye registro e inicio de sesión, autenticación JWT, protección de endpoints y aislamiento de los recursos asociados a los usuarios. La suite completa de tests pasó tras los cambios de autenticación.

Queda pendiente la revisión final de los criterios de aceptación, la documentación y la confirmación de que todos los puntos de seguridad previstos estén cubiertos.

#### Etapa A --- Diseño e inspección

-   [x] Actualizar `main` y revisar `pom.xml`, configuración de
    seguridad existente, entidades, DTOs, servicios, controladores y
    perfiles.
-   [x] Confirmar dependencias y compatibilidad de Spring Security/JWT
    con la versión actual de Spring Boot.
-   [x] Definir el modelo de usuario y el identificador único de inicio
    de sesión (por ejemplo, email).
-   [x] Definir qué recursos pertenecen a cada usuario y cómo se
    vinculará el propietario a los almacenamientos y movimientos.
-   [x] Decidir qué endpoints serán públicos y cuáles requerirán
    autenticación.

#### Etapa B --- Registro y login

-   [x] Crear entidad de usuario, repositorio y restricciones de
    unicidad.
-   [x] Crear DTOs de registro y login con validaciones.
-   [x] Implementar registro e inicio de sesión.
-   [x] Guardar contraseñas con un `PasswordEncoder` seguro, por ejemplo
    BCrypt; nunca almacenar ni devolver contraseñas en texto plano.
-   [x] Manejar duplicados y credenciales inválidas con respuestas HTTP
    apropiadas.
-   [x] Añadir pruebas unitarias y de integración.

#### Etapa C --- JWT y Spring Security

-   [x] Implementar generación y validación de JWT con expiración.
-   [x] Leer la clave/secreto desde configuración externa; no versionar
    secretos reales.
-   [x] Configurar Spring Security para autenticar solicitudes mediante
    `Authorization: Bearer <token>`.
-   [x] Mantener el registro, login y los recursos públicos
    explícitamente permitidos sin autenticación.
-   [x] Proteger los endpoints privados.
-   [x] Definir respuestas coherentes para token ausente, inválido o
    vencido.
-   [x] Añadir pruebas para autenticación exitosa y fallida, y para
    endpoints públicos y protegidos.

#### Etapa D --- Autorización y aislamiento de datos

-   [x] Aplicar la propiedad de usuario a los recursos definidos para el
    MVP.
-   [x] Validar la autorización en la lógica de negocio y las consultas;
    no depender solamente de ocultar rutas en el controlador.
-   [x] Evitar que un usuario acceda a recursos de otro modificando un
    ID de la URL.
-   [x] Añadir pruebas donde el usuario A intenta consultar o modificar
    recursos del usuario B.
-   [x] Revisar especialmente almacenamientos, inventario, ingresos y
    egresos para no filtrar datos entre usuarios.
-   [x] Documentar las decisiones de seguridad y los cambios de modelo
    necesarios.

**Criterios de aceptación:**

-   Un usuario puede registrarse e iniciar sesión.
-   Las contraseñas se guardan hasheadas y nunca se exponen en
    respuestas.
-   Un JWT válido permite acceder a endpoints protegidos; un token
    ausente, inválido o vencido no.
-   Los endpoints públicos y privados están definidos explícitamente.
-   Un usuario no puede consultar ni modificar los recursos de otro
    usuario.
-   Las pruebas automatizadas cubren registro, login, JWT y aislamiento
    entre usuarios.

**Alcance deliberadamente acotado:** para la primera versión basta un
tipo de usuario común. No implementar OAuth, refresh tokens,
recuperación de contraseña, verificación de email ni roles complejos
salvo que aparezca una necesidad concreta. JWT y autorización deben
desarrollarse como partes relacionadas de esta misma iniciativa,
posiblemente en commits o PRs pequeños.

### FM-043 --- Historial y consultas de movimientos

**Prioridad:** alta · **Tipo:** funcionalidad/API · **Estado:**
pendiente

-   [ ] Inspeccionar los endpoints y repositorios actuales de ingresos y
    egresos para detectar qué consultas ya existen.
-   [ ] Definir un contrato coherente para consultar movimientos por
    tipo, alimento, almacenamiento y período.
-   [ ] Implementar filtros, ordenamiento y paginación si corresponde al
    volumen esperado.
-   [ ] Asegurar que el detalle histórico conserve la cantidad y unidad
    originales solicitadas, aunque el inventario use cantidades
    normalizadas.
-   [ ] Integrar la autorización de FM-045 para que cada usuario solo
    vea los movimientos permitidos.
-   [ ] Añadir pruebas para filtros, límites de fechas, orden,
    paginación y aislamiento de datos.
-   [ ] Documentar los parámetros y ejemplos de respuesta.

**Criterios de aceptación:** un usuario autenticado puede consultar un
historial útil con filtros documentados y no puede acceder a movimientos
de otro usuario.

### FM-046 --- Docker y ejecución reproducible

**Prioridad:** muy alta · **Tipo:** infraestructura · **Estado:**
pendiente

-   [ ] Crear un `Dockerfile` adecuado para empaquetar y ejecutar la
    API.
-   [ ] Crear `compose.yaml` con la API y PostgreSQL.
-   [ ] Configurar URL y credenciales de base de datos, secreto JWT y
    demás valores mediante variables de entorno.
-   [ ] Añadir `.env.example` con placeholders no sensibles; no
    versionar secretos reales.
-   [ ] Configurar persistencia de PostgreSQL mediante un volumen.
-   [ ] Configurar una estrategia razonable de
    disponibilidad/healthcheck para PostgreSQL y el arranque de la API.
-   [ ] Comprobar que las pruebas continúen ejecutándose con el perfil
    `test` y H2, sin depender de Docker.
-   [ ] Validar la ejecución desde un entorno limpio siguiendo las
    instrucciones del README.

**Criterios de aceptación:** con Docker instalado y las variables
configuradas, `docker compose up --build` inicia la API y PostgreSQL; se
puede completar un flujo de registro, login y acceso autorizado.

### FM-047 --- Configuración segura y migraciones de base de datos

**Prioridad:** alta antes de una demo pública · **Tipo:**
seguridad/operaciones · **Estado:** pendiente

-   [ ] Revisar credenciales, secretos, perfiles y configuración CORS.
-   [ ] Confirmar que no haya secretos reales ni datos sensibles
    versionados.
-   [ ] Evaluar Flyway o Liquibase para versionar el esquema en vez de
    depender de actualización automática.
-   [ ] Definir una configuración separada para desarrollo y tests, y
    una configuración segura para una futura demo.
-   [ ] Documentar límites conocidos y evitar exponer configuración de
    desarrollo a Internet.

**Criterios de aceptación:** secretos externalizados, esquema
reproducible y configuración documentada. No hace falta desplegar
públicamente para cerrar esta tarea.

### FM-048 --- Documentación y demo de portfolio

**Prioridad:** alta · **Tipo:** documentación/portfolio · **Estado:**
pendiente

-   [ ] Actualizar el README con propósito, stack, arquitectura y
    funcionalidades implementadas.
-   [ ] Documentar requisitos e instrucciones para ejecutar localmente y
    con Docker.
-   [ ] Documentar endpoints, DTOs, códigos de respuesta y errores
    mediante OpenAPI/Swagger.
-   [ ] Añadir ejemplos de registro, login, uso del JWT y consultas del
    historial.
-   [ ] Documentar las decisiones relevantes de seguridad: contraseñas
    hasheadas, autorización y aislamiento por usuario.
-   [ ] Incluir ejemplos de configuración con placeholders, nunca
    secretos reales.
-   [ ] Verificar que una persona pueda clonar y ejecutar el proyecto
    siguiendo solamente el README.

**Criterios de aceptación:** una persona técnica puede clonar el
repositorio, iniciar el proyecto, registrar un usuario, autenticarse,
operar el inventario y consultar el historial siguiendo la
documentación.

### FM-049 --- Revisión final de robustez del inventario

**Prioridad:** media-alta · **Tipo:** calidad/backend · **Estado:**
pendiente de reevaluar

-   [ ] Revisar si la protección ante solicitudes concurrentes que
    consumen el mismo stock está cubierta por la implementación y los
    tests existentes.
-   [ ] Si hay una brecha real, elegir una estrategia apropiada (por
    ejemplo, bloqueo optimista/pesimista o actualización atómica).
-   [ ] Añadir pruebas para la estrategia elegida; no confundir rollback
    transaccional con una prueba real de concurrencia.
-   [ ] Documentar la decisión y los límites restantes.

**Criterios de aceptación:** los tests y la implementación respaldan la
protección ante egresos concurrentes. No duplicar trabajo si ya está
resuelto; inspeccionar el código antes de actuar.

### FM-050 --- Paginación y filtros generales

**Prioridad:** media · **Tipo:** API · **Estado:** pendiente de
reevaluar

-   [ ] Revisar los listados que todavía devuelvan colecciones
    completas.
-   [ ] Añadir paginación y orden donde resulte útil y no esté ya
    cubierto por FM-043.
-   [ ] Mantener filtros por estado activo y agregar otros solo si
    responden a una necesidad concreta.
-   [ ] Añadir tests para parámetros inválidos y resultados paginados.

**Criterios de aceptación:** los listados que pueden crecer se consultan
de forma controlada y documentada. Evitar duplicar el alcance del
historial.

## 5. Orden recomendado de implementación

1. **FM-045 — Usuarios, JWT y autorización.** Completar la revisión final de seguridad, verificar los criterios de aceptación pendientes y actualizar la documentación antes de cerrar la iniciativa.
2.  **FM-043 --- Historial de movimientos.** Completar las consultas y
    asegurar que respeten la autorización por usuario.
3.  **FM-046 --- Docker y PostgreSQL.** Preparar una ejecución
    reproducible del flujo completo.
4.  **FM-047 --- Configuración segura y migraciones.** Resolver lo
    necesario para que el proyecto sea consistente y esté preparado para
    una demo.
5.  **FM-048 --- README y demo de portfolio.** Dejar instrucciones
    claras para reclutadores y otros desarrolladores.
6.  **FM-049 --- Robustez del inventario.** Confirmar si queda trabajo
    real en concurrencia y resolver brechas importantes.
7.  **FM-050 --- Paginación general.** Completar los listados que
    realmente lo necesiten.

El orden puede ajustarse si la inspección del código descubre
dependencias técnicas. No asumir que una función falta ni duplicar
trabajo sin comprobar primero la implementación actual.

## 6. Forma de trabajo acordada

Para cada tarea:

1.  Confirmar objetivo y criterios de aceptación antes de tocar código.
2.  Partir de `main` actualizado y crear una rama específica.
3.  Revisar el código existente antes de proponer cambios.
4.  Implementar paso a paso, explicando decisiones y dejando que Seba
    escriba el código.
5.  Añadir o actualizar tests.
6.  Ejecutar `./mvnw clean test`.
7.  Revisar `git diff`, `git diff --check` y `git status`.
8.  Hacer un commit con un mensaje claro.
9.  Subir la rama, abrir PR y revisar las comprobaciones antes del
    merge.
10. Después del merge, volver a `main`, actualizarla, verificar que el
    árbol esté limpio y actualizar este `BACKLOG.md`.

## 7. Comandos habituales de Git

``` bash
git switch main
git pull origin main
git switch -c feat/nombre-de-la-tarea

# Después de implementar y probar:
./mvnw clean test
git status
git diff
git diff --check
git add <archivos>
git diff --cached --check
git diff --cached --stat
git commit -m "tipo: describe el cambio"
git push -u origin feat/nombre-de-la-tarea
```

Después del merge, volver a `main`, actualizarla y verificar que el
árbol esté limpio.

## 8. Próxima acción

Completar la revisión de **FM-045 — Registro de usuarios, autenticación JWT y autorización** en la rama `feat/user-authentication`.

1. Revisar los criterios de aceptación y los checks pendientes.
2. Verificar el aislamiento de datos entre usuarios y el tratamiento de tokens inválidos, vencidos o asociados a usuarios inexistentes.
3. Actualizar la documentación de seguridad y uso de la autenticación.
4. Ejecutar `./mvnw clean test`.
5. Revisar `git diff`, `git diff --check` y `git status`.
6. Preparar el commit y continuar con el flujo de revisión y merge acordado.