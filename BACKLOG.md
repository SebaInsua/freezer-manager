# Freezer Manager — Roadmap y backlog

> Documento de continuidad del proyecto. Actualizarlo al cerrar cada tarea y subir los cambios a GitHub.
>
> **Fuente del estado actual:** código y Git del repositorio revisados el 23/10/2026.  
> **Importante:** este archivo reconstruye el backlog a partir del estado real del proyecto y sus objetivos documentados. No se pudo recuperar palabra por palabra el mapa anterior de ChatGPT; las tareas futuras están marcadas como propuestas y deben priorizarse antes de implementarlas.

## 1. Contexto del proyecto

**Freezer Manager** es una API REST para ayudar a un hogar a administrar alimentos, inventario del freezer, compras, preparación de comidas e historial de movimientos.

### Stack confirmado
- Java 21
- Spring Boot 4
- Maven
- Spring Data JPA / Hibernate
- PostgreSQL para desarrollo
- H2 y perfil `test` para pruebas
- JUnit y Mockito; tests de integración con Spring Boot / MockMvc

### Arquitectura actual
- `domain`: entidades y enums de alimentos e inventario.
- `dto`: requests y respuestas de la API.
- `repository`: persistencia con Spring Data JPA.
- `service`: lógica de negocio.
- `controller`: endpoints REST.
- `exception`: excepciones y manejo global de errores.

## 2. Estado actual — completado

Estas funcionalidades están presentes en `main` según el historial de Git y el código revisado.

- [x] Estructura inicial del proyecto y configuración de Spring Boot.
- [x] Perfiles separados para desarrollo y tests.
- [x] Modelo de alimentos, categorías y unidades de medida.
- [x] Modelo de depósitos/ubicaciones de almacenamiento.
- [x] Modelo de movimientos: ingresos, egresos y sus detalles.
- [x] Validaciones de dominio y restricciones de entidades.
- [x] Repositorios JPA.
- [x] Servicios de alimentos, depósitos, ítems de inventario e ingresos/egresos.
- [x] Endpoints REST para alimentos, depósitos, ítems y movimientos.
- [x] Manejo global de errores y respuestas estructuradas.
- [x] Alta, actualización, desactivación y filtrado de alimentos activos.
- [x] Prevención de movimientos con alimentos inactivos.
- [x] Registro de ingresos y actualización del inventario.
- [x] Registro de egresos y descuento del inventario.
- [x] Respuesta HTTP 400 para errores de negocio aplicables.
- [x] Normalización de cantidades: KG a gramos; G y UNIT conservan su escala.
- [x] Validación de unidades permitidas para cada alimento.
- [x] Eliminación del `StorageItem` cuando el stock llega a cero, dado que su cantidad debe ser positiva.
- [x] Modularización de la construcción de detalles de ingreso y egreso mediante `StorageEntryDetailService` y `StorageExitDetailService`.
- [x] Extracción de conversión de unidades en `UnitConversionService`.
- [x] Tests unitarios y de integración para las funcionalidades anteriores.
- [x] Test de integración que verifica rollback de un egreso con varios detalles cuando un detalle no tiene stock suficiente.
- [x] Merge a `main` del PR #37 (`refactor: modularize entry and exit detail creation`).
- [x] Última ejecución reportada por el usuario: `mvn clean test`, todos los tests aprobados.
- [x] Repositorio limpio después del merge.

### Último hito de Git
- Commit de la tarea: `99fd479` — `refactor: modularize entry and exit detail creation`
- PR integrado: #37
- Rama de trabajo integrada: `refactor/storage-exit-detail-service`

## 3. Backlog próximo — orden sugerido

Las tareas siguientes son una propuesta basada en los objetivos del README y en la funcionalidad existente. Confirmar el alcance de cada una antes de comenzar.

### FM-035 — Documentar y verificar el contrato actual de la API
**Prioridad:** alta · **Tipo:** documentación/calidad

- [ ] Revisar los endpoints, métodos HTTP, DTOs, validaciones y códigos de respuesta reales.
- [ ] Añadir documentación OpenAPI/Swagger si todavía no está configurada.
- [ ] Documentar ejemplos de requests y responses, incluidos errores.
- [ ] Verificar que la documentación coincida con el comportamiento de los tests.
- [ ] Ejecutar `mvn clean test`.

**Criterio de aceptación:** una persona puede entender y probar los endpoints sin leer la implementación de los servicios.

### FM-036 — Revisión de reglas de negocio e inventario
**Prioridad:** alta · **Tipo:** calidad/backend

- [ ] Revisar validaciones de cantidades nulas, cero y negativas en requests.
- [ ] Verificar que los alimentos solo acepten las unidades compatibles.
- [ ] Verificar cantidades canónicas para KG, G y UNIT en ingresos y egresos.
- [ ] Confirmar comportamiento cuando el stock queda exactamente en cero.
- [ ] Confirmar que un egreso inválido no deje cambios parciales.
- [ ] Añadir tests para cualquier caso límite que falte.

**Criterio de aceptación:** reglas documentadas y cubiertas por tests unitarios o de integración.

### FM-037 — Consistencia y concurrencia del inventario
**Prioridad:** alta · **Tipo:** robustez

- [ ] Evaluar qué sucede si dos egresos intentan consumir el mismo stock simultáneamente.
- [ ] Elegir una estrategia apropiada (por ejemplo, bloqueo optimista/pesimista o actualización atómica) según el modelo actual.
- [ ] Añadir pruebas que validen la estrategia elegida; no confundir rollback de una transacción con una prueba de concurrencia real.
- [ ] Documentar la decisión técnica.

**Criterio de aceptación:** el sistema no permite retirar más unidades de las disponibles bajo solicitudes concurrentes.

### FM-038 — Paginación, orden y filtros de listados
**Prioridad:** media · **Tipo:** API

- [ ] Revisar listados que actualmente devuelven colecciones completas.
- [ ] Incorporar paginación y orden donde resulte útil.
- [ ] Mantener filtros por estado activo y agregar otros solo si responden a necesidades concretas.
- [ ] Añadir tests de parámetros inválidos y resultados paginados.

**Criterio de aceptación:** los listados pueden consultarse de forma controlada y documentada.

### FM-039 — Inventario por lotes
**Prioridad:** media · **Tipo:** funcionalidad de dominio

El README original menciona administrar inventario por lotes. Antes de implementarlo, definir qué representa un lote (fecha de ingreso, vencimiento, cantidad, ubicación, costo u otros datos).

- [ ] Definir el modelo de lote y sus reglas.
- [ ] Diseñar cómo se vinculan los ingresos con los lotes.
- [ ] Definir cómo se descuentan lotes al registrar un egreso (por ejemplo, primero vence, primero sale, si corresponde).
- [ ] Planificar migración y compatibilidad con el inventario actual.
- [ ] Implementar y probar en tareas pequeñas.

**Criterio de aceptación:** se puede rastrear cuánto stock corresponde a cada lote y cómo se consume.

### FM-040 — Fechas de vencimiento y alertas
**Prioridad:** media · **Tipo:** funcionalidad de dominio

- [ ] Definir si el vencimiento se registra por lote o por alimento.
- [ ] Crear consultas para alimentos próximos a vencer y vencidos.
- [ ] Definir umbrales configurables para alertas.
- [ ] Añadir tests para límites de fechas.

**Dependencia:** definir primero el modelo de lotes si el vencimiento pertenece a cada partida.

### FM-041 — Capacidad del freezer y espacio disponible
**Prioridad:** media · **Tipo:** funcionalidad de dominio

El README menciona estimar capacidad y espacio libre.

- [ ] Definir cómo se expresa la capacidad (volumen, peso u otra medida).
- [ ] Determinar qué datos debe tener cada depósito/freezer.
- [ ] Definir cómo se estima el espacio ocupado y sus limitaciones.
- [ ] Implementar consultas y validaciones sin asumir que peso equivale a volumen.

**Criterio de aceptación:** la API ofrece una estimación explícita y documentada de capacidad utilizada y disponible.

### FM-042 — Compras de alimentos
**Prioridad:** media · **Tipo:** funcionalidad

El README menciona registrar compras y cantidades.

- [ ] Definir si una compra crea automáticamente un ingreso de inventario o requiere confirmación.
- [ ] Modelar compra, detalle, precio y fecha solo según el alcance acordado.
- [ ] Definir cómo se relacionan compras con alimentos y movimientos de ingreso.
- [ ] Añadir endpoints y tests.

### FM-043 — Historial y consultas de movimientos
**Prioridad:** media · **Tipo:** API

- [ ] Revisar consultas de historial de ingresos y egresos por alimento, depósito y período.
- [ ] Definir filtros y orden.
- [ ] Asegurar que el detalle histórico conserve la cantidad y unidad originales solicitadas, aunque el inventario use cantidades normalizadas.
- [ ] Añadir tests para filtros y límites de fechas.

### FM-044 — Preparación de comidas y consumo
**Prioridad:** posterior · **Tipo:** funcionalidad de producto

El README menciona preparación de comidas y registro de consumo.

- [ ] Definir el concepto de comida/receta y sus ingredientes.
- [ ] Definir si una receta puede descontar automáticamente el inventario.
- [ ] Diseñar el registro de preparación y consumo, evitando descontar stock dos veces.
- [ ] Implementar una primera versión acotada y testeada.

### FM-045 — Seguridad y preparación para despliegue
**Prioridad:** antes de exponer la API públicamente · **Tipo:** infraestructura/seguridad

- [ ] Revisar gestión de credenciales mediante variables de entorno.
- [ ] Confirmar que secretos y contraseñas no estén versionados.
- [ ] Evaluar autenticación y autorización según quién usará la API.
- [ ] Revisar CORS, configuración de producción y perfiles.
- [ ] Evaluar migraciones de base de datos (Flyway o Liquibase) en lugar de depender de actualización automática del esquema.
- [ ] Preparar Docker y documentación de ejecución si el despliegue lo requiere.

## 4. Forma de trabajo acordada

Para cada tarea:

1. Confirmar objetivo y criterios de aceptación antes de tocar código.
2. Partir de `main` actualizado y crear una rama específica.
3. Revisar el código existente antes de proponer cambios.
4. Implementar paso a paso, explicando decisiones y dejando que Seba escriba el código.
5. Añadir o actualizar tests.
6. Ejecutar `mvn clean test`.
7. Revisar `git diff` y `git status`.
8. Hacer un commit con un mensaje claro.
9. Subir la rama, abrir PR y hacer merge.
10. Actualizar este `BACKLOG.md` con el estado real y el próximo paso.

## 5. Comandos habituales de Git

```bash
git switch main
git pull
git switch -c feat/nombre-de-la-tarea

# Después de implementar y probar:
mvn clean test
git status
git diff
git add .
git diff --cached --stat
git commit -m "tipo: describe el cambio"
git push -u origin feat/nombre-de-la-tarea
```

Después del merge, volver a `main`, actualizarla y verificar que el árbol esté limpio.

## 6. Próxima acción

Empezar por **FM-035: documentar y verificar el contrato actual de la API**, pero primero inspeccionar el `pom.xml` y los controladores para confirmar si OpenAPI/Swagger ya está configurado. Si el objetivo anterior del usuario o el mapa original recuperado indica otra prioridad, actualizar este orden antes de implementar.
