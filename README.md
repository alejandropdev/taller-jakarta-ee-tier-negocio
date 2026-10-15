# Taller No.2 – Jakarta EE (Tier de Negocio)

Módulo de gestión de solicitudes de recursos en emergencias sobre **Jakarta EE 10 + WildFly 37**.
El tier de negocio son **EJB de sesión sin estado** y queda desacoplado del mecanismo de
persistencia mediante un contrato declarado en el dominio.

*Arquitectura de Software · Pontificia Universidad Javeriana.*

| Documento | Contenido |
|-----------|-----------|
| [docs/architecture/](docs/architecture/README.md) | Diagramas de componentes, secuencias, estados y la explicación del desacople (PDF en [pdf/](docs/architecture/pdf/)) |
| [docs/evaluacion-final.md](docs/evaluacion-final.md) | Cada requisito contra el código y su evidencia |

## Estructura

```
rm-domain    Entidad, contrato de repositorio, excepciones y DTO. No depende de nadie.
rm-business  EJB de sesión con las reglas de negocio + vistas @Local y @Remote.
rm-data      Implementación JPA del contrato del dominio.
rm-web       Fachada REST sobre la vista local del EJB.
rm-ear       Empaquetado desplegable.
rm-client    Cliente en otra JVM que invoca la vista remota por JNDI.
```

El EAR desplegado contiene `lib/rm-domain.jar`, `rm-business.jar`, `rm-data.jar` y `rm-web.war`.

## Requisitos

- Docker con Docker Compose v2. Para levantar el servidor no hace falta Java ni Maven: el
  `Dockerfile` compila el proyecto y ejecuta las pruebas unitarias.
- Java 17 o superior y Maven, solo para ejecutar el cliente remoto desde la máquina local.
- Puertos libres: `8080` (API y EJB remoto) y `9990` (consola de WildFly).
- Para las pruebas de extremo a extremo: `bash`, `curl` y `jq`.

## Ejecución

```bash
docker compose up -d --build    # compila, ejecuta las pruebas unitarias y despliega el EAR
docker compose logs -f wildfly  # traza del despliegue y los nombres JNDI publicados
docker compose down             # detener (H2 es en memoria: la base arranca vacía cada vez)
```

La API queda en `http://localhost:8080/api`.

## API

| Operación | Petición | Respuesta |
|-----------|----------|-----------|
| Registrar | `POST /api/resource-requests` | `201` con `Location` y la solicitud en estado `PENDIENTE` |
| Consultar todas | `GET /api/resource-requests` | `200` con el arreglo de solicitudes |
| Consultar por id | `GET /api/resource-requests/{id}` | `200`, o `404` si no existe |
| Consultar pendientes | `GET /api/resource-requests/pending` | `200` con las que siguen en `PENDIENTE` |
| Atender | `POST /api/resource-requests/{id}/fulfillment` | `200` con estado `ATENDIDA`, `404` si no existe, `409` si ya fue atendida |

Cuerpo de registro:

```json
{
  "disasterDescription": "Inundación en la ribera del río",
  "resourceType": "Agua potable",
  "quantity": 250,
  "priority": "ALTA"
}
```

Las validaciones no están en este cuerpo ni en el recurso REST: las hace el EJB de sesión.

## Cliente remoto (EJB por JNDI)

```bash
mvn -q install -DskipTests     # publica los módulos en el repositorio local
mvn -q -pl rm-client exec:java
```

Invoca `ResourceRequestServiceRemote` con el nombre
`ejb:resource-management/rm-business/ResourceRequestServiceBean!co.edu.javeriana.rm.business.ResourceRequestServiceRemote`
sobre `http-remoting://localhost:8080`, autenticándose con el usuario de aplicación `rmclient`
que crea la imagen. Las credenciales y la URL se pueden cambiar con `-Drm.user`, `-Drm.password`
y `-Drm.provider.url`.

## Pruebas

| Tipo | Comando | Cobertura |
|------|---------|-----------|
| Unitarias | `mvn -B test` | 24 pruebas: las reglas de negocio con el repositorio simulado y los invariantes de la entidad |
| Extremo a extremo | `./scripts/pruebas-e2e.sh` | 28 comprobaciones sobre el stack levantado, incluida la invocación remota |

`SIN_CLIENTE=1 ./scripts/pruebas-e2e.sh` omite la parte del cliente remoto.

## Documentación en PDF

```bash
./scripts/render-docs.sh        # regenera docs/architecture/pdf/*.pdf
```

Convierte los diagramas Mermaid con `mermaid-cli`, arma el HTML con pandoc e imprime el PDF con
Chrome en modo headless.
