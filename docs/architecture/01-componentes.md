# 01 – Diagrama de componentes

Los tres tiers y el contenido del EAR desplegado en WildFly.

```mermaid
flowchart LR
    ClienteHTTP([Cliente HTTP<br/>curl · navegador])
    ClienteRMI([Cliente Java<br/>otra JVM])

    subgraph WF["WildFly 37 · resource-management.ear"]
        direction TB

        subgraph WEB["rm-web.war · Tier de presentación"]
            REST["ResourceRequestResource<br/>JAX-RS /api/resource-requests"]
            MAP["ApiExceptionMapper<br/>excepción → código HTTP"]
        end

        subgraph BUS["rm-business.jar · Tier de NEGOCIO"]
            LOCAL(["@Local<br/>ResourceRequestService"])
            REMOTE(["@Remote<br/>ResourceRequestServiceRemote"])
            BEAN["ResourceRequestServiceBean<br/>@Stateless · REQUIRED<br/>reglas de negocio"]
        end

        subgraph LIB["lib/rm-domain.jar · Dominio compartido"]
            ENT["ResourceRequest<br/>@Entity con invariantes"]
            REPO(["ResourceRequestRepository<br/>contrato de persistencia"])
            DTO["DTO y excepciones de negocio"]
        end

        subgraph DAT["rm-data.jar · Tier de DATOS"]
            JPA["JpaResourceRequestRepository<br/>@Stateless · MANDATORY<br/>EntityManager"]
        end

        TM[["Transacciones JTA<br/>Narayana"]]
    end

    DB[("H2 embebido<br/>ResourceManagementDS")]

    ClienteHTTP -->|HTTP/JSON| REST
    ClienteRMI -->|JNDI · http-remoting:8080| REMOTE
    REST --> LOCAL
    LOCAL --> BEAN
    REMOTE --> BEAN
    BEAN -->|invoca el contrato| REPO
    JPA -.implementa.-> REPO
    BEAN --> ENT
    JPA --> ENT
    JPA --> DB
    BEAN -.- TM
    REST -.- MAP
    BEAN --> DTO
```

El tier de negocio es el único que conoce las reglas. El tier de presentación deserializa y
traduce errores; el tier de datos solo lee y escribe.
