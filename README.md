# Taller No.2 – Jakarta EE (Tier de Negocio)

Módulo de gestión de solicitudes de recursos en emergencias. El tier de negocio son EJB de sesión
sin estado sobre Jakarta EE 10 y WildFly 37, desacoplados del mecanismo de persistencia.

*Arquitectura de Software · Pontificia Universidad Javeriana.*
*Autores: Alejandro Parrado y Diego Jimenez.*

## Entregables

### 1. Código fuente

| Módulo | Contenido |
|--------|-----------|
| `rm-domain` | Entidad, contrato de repositorio, excepciones y DTO |
| `rm-business` | EJB de sesión con las reglas de negocio, vistas `@Local` y `@Remote` |
| `rm-data` | Implementación JPA del contrato del dominio |
| `rm-web` | Fachada REST |
| `rm-ear` | Empaquetado desplegable |
| `rm-client` | Cliente en otra JVM que invoca el EJB por JNDI |

### 2. Diseño de la arquitectura

| Documento | Contenido |
|-----------|-----------|
| [docs/architecture/](docs/architecture/README.md) | Índice, stack y justificación del tipo de EJB |
| [01-componentes.md](docs/architecture/01-componentes.md) | Componentes y contenido del EAR |
| [**02-desacople-negocio-datos.md**](docs/architecture/02-desacople-negocio-datos.md) | **Cómo se desacopló el tier de negocio del tier de datos** |
| [03-secuencia-registrar.md](docs/architecture/03-secuencia-registrar.md) · [04-secuencia-atender.md](docs/architecture/04-secuencia-atender.md) | Flujos de las dos operaciones de escritura |
| [05-estados.md](docs/architecture/05-estados.md) | Máquina de estados y dónde se hace cumplir cada regla |
| [06-modelo-datos.md](docs/architecture/06-modelo-datos.md) | Tabla, unidad de persistencia y datasource |

Los mismos documentos renderizados están en [docs/architecture/pdf/](docs/architecture/pdf/).

### 3. Evidencias de funcionamiento

| Evidencia | Cómo reproducirla |
|-----------|-------------------|
| 24 pruebas unitarias de las reglas de negocio | `mvn -B test` |
| 28 comprobaciones sobre el módulo desplegado | `docker compose up -d --build` y luego `./scripts/pruebas-e2e.sh` |
| Invocación remota del EJB por JNDI | `mvn -q install -DskipTests` y luego `mvn -q -pl rm-client exec:java` |

## Requisitos para ejecutar

Docker con Compose v2 y los puertos `8080` y `9990` libres. Para el cliente remoto y las pruebas
unitarias, Java 17 o superior y Maven. Para el script de extremo a extremo, `curl` y `jq`.

La API queda en `http://localhost:8080/api`; los endpoints están en
[01-componentes.md](docs/architecture/01-componentes.md) y en el propio recurso REST.
