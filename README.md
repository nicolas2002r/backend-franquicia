# FRANCHISES API (SPRING BOOT WEBFLUX + CLEAN ARCHITECTURE SCAFFOLD)

API reactiva para administrar **Franquicias → Sucursales → Productos (stock)**, construida con **Spring Boot WebFlux** siguiendo **Arquitectura Hexagonal / Clean Architecture (Scaffold Bancolombia)**, con persistencia **MongoDB Reactive**, pruebas unitarias y empaquetado con Docker.

---

## CONTENIDO
- [1. REQUERIMIENTOS FUNCIONALES](#1-requerimientos-funcionales)
- [2. TECNOLOGÍAS](#2-tecnologías)
- [3. ARQUITECTURA (HEXAGONAL / CLEAN)](#3-arquitectura-hexagonal--clean)
- [4. PATRONES DE DISEÑO APLICADOS](#4-patrones-de-diseño-aplicados)
- [5. REACTIVO: OPERADORES Y SEÑALES](#5-reactivo-operadores-y-señales)
- [6. LOGGING / AUDITORÍA](#6-logging--auditoría)
- [7. PERSISTENCIA (MONGODB REACTIVE)](#7-persistencia-mongodb-reactive)
- [8. API REST (ENDPOINTS)](#8-api-rest-endpoints)
- [9. EJECUCIÓN LOCAL](#9-ejecución-local)
- [10. DOCKER](#10-docker)
- [11. PRUEBAS Y COBERTURA](#11-pruebas-y-cobertura)
- [12. INFRAESTRUCTURA COMO CÓDIGO (TERRAFORM) - OPCIONAL](#12-infraestructura-como-código-terraform---opcional)
- [13. CONSIDERACIONES DE DISEÑO](#13-consideraciones-de-diseño)

---

## 1. REQUERIMIENTOS FUNCIONALES

1. Crear una franquicia.
2. Agregar una sucursal a una franquicia.
3. Agregar un producto a una sucursal.
4. Eliminar un producto de una sucursal.
5. Modificar el stock de un producto.
6. Consultar el producto con mayor stock por sucursal para una franquicia (retorna lista con sucursal + producto).
7. (Plus) Actualizar nombre de franquicia.
8. (Plus) Actualizar nombre de sucursal.
9. (Plus) Actualizar nombre de producto.

---

## 2. TECNOLOGÍAS

- Java 25
- Spring Boot 2.7.x
- Spring WebFlux (Reactive)
- Reactor (Mono/Flux)
- MongoDB Reactive (spring-boot-starter-data-mongodb-reactive)
- Validación: `spring-boot-starter-validation` (Jakarta Validation)
- Logging: SLF4J + Logback (en infraestructura)
- Tests: JUnit 5 + Mockito + Reactor Test (StepVerifier)
- Docker / Docker Compose

---

## 3. ARQUITECTURA (HEXAGONAL / CLEAN)

Se usa la estructura típica del **Scaffold Clean Architecture**:

- **domain/model**
    - Entidades del dominio (Franchise, Branch, Product, MaxStockByBranch)
    - Excepciones (NotFoundException, ValidationException, ConflictException)
    - **Gateways** (interfaces) para persistencia y auditoría:
        - `FranchiseRepository`, `BranchRepository`, `ProductRepository`, `AuditLogger`

- **domain/usecase**
    - Casos de uso (reglas de negocio + orquestación)
    - No depende de frameworks (Spring, SLF4J, etc.)

- **infrastructure/driven-adapters**
    - Implementaciones de gateways:
        - `MongoRepositoryAdapter` (ReactiveMongoRepository) implementa los repos del dominio

- **infrastructure/entry-points**
    - Exposición HTTP WebFlux:
        - Router + Handler funcional (o Controller, según preferencia)
    - DTOs Request/Response + Mapper (transformaciones)

### RESTRICCIONES IMPORTANTES DEL SCAFFOLD
- `usecase` **solo** depende de `model`.
- `model` **no** debe traer SLF4J/loggers.
- El logging técnico se hace en infraestructura (entry-points / adapters).
- Para cumplir el criterio de “logging” sin romper reglas, se usa un **Gateway** `AuditLogger` (en model) implementado en infraestructura con SLF4J.

---

## 4. PATRONES DE DISEÑO APLICADOS

### COMMAND PATTERN (RECOMENDADO PARA ESTE CASO)
Cada operación del negocio se encapsula en una clase pequeña (**1 clase = 1 operación**):
- CreateFranchiseCommand
- AddBranchCommand
- AddProductCommand
- UpdateProductStockCommand
- etc.

Beneficios:
- Menos “God class” en el UseCase.
- Más mantenible y testeable.
- Facilita cobertura alta: pruebas por operación.

### FACADE / ORCHESTRATOR (FranchiseUseCase)
`FranchiseUseCase` actúa como fachada, delegando a los commands y exponiendo una API coherente para el entry-point.

### MAPPER (DTO ↔ DOMINIO)
Transformación explícita entre:
- Request DTO → inputs usecase
- Dominio → Response DTO

---

## 5. REACTIVO: OPERADORES Y SEÑALES

Se usan operadores Reactor para cumplir el encadenamiento de flujos:

- `map`: transformar valores (ej: trim)
- `flatMap`: encadenar operaciones async (repo.save, repo.exists)
- `switchIfEmpty`: controlar vacíos (ej: not found / validación)
- `merge`: ejecutar 2 flujos en paralelo (ej: guardar + auditar)
- `zip`: combinar resultados (ej: branch + producto top stock)
- `thenReturn / then`: continuidad del flujo sin romper reactividad

### SEÑALES: onNext / onError / onComplete
En vez de hacer `subscribe()` dentro del flujo (anti-pattern), se encadena auditoría de forma reactiva con un soporte tipo:

- `flatMap(x -> audit.info(...).thenReturn(x))`
- `onErrorResume(e -> audit.error(...).then(Mono.error(e)))`
- Para `Flux`, se agrega una auditoría al final del flujo (equivalente conceptual a `onComplete`).

---

## 6. LOGGING / AUDITORÍA

### LOGGING TÉCNICO (HTTP)
- Se recomienda un `WebFilter` en entry-points para registrar request/response (método, path, latencia, error).

Paquete sugerido:
- `co.com.bancolombia.api.config.LoggingWebFilter`

### AUDITORÍA DE NEGOCIO (desde UseCase sin SLF4J)
- Se define un gateway en model:
    - `co.com.bancolombia.model.gateways.AuditLogger`
- Se implementa en infraestructura con SLF4J:
    - `co.com.bancolombia.api.logger.AuditLoggerAdapter`

Esto permite cumplir:
- logging adecuado
- sin violar dependencias del scaffold

---

## 7. PERSISTENCIA (MONGODB REACTIVE)

### ¿Por qué MongoDB?
- Modelo jerárquico: franquicia/sucursal/producto suele mapear bien en documentos o colecciones relacionadas.
- Soporte oficial reactivo con Spring Data: `spring-boot-starter-data-mongodb-reactive`
- No bloquea el event-loop, cumple reactividad end-to-end.

### Colecciones sugeridas
- `franchises`
- `branches` (con `franchiseId`)
- `products` (con `branchId`)

---

## 8. API REST (ENDPOINTS)

Base path: `/api/v1`

### 8.1 Crear franquicia
**POST** `/franchises`
```json
{ "name": "Franquicia A" }
