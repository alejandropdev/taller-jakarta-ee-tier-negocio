# 02 – Desacople entre el tier de negocio y el de datos

Logramos el desacople haciendo que las dependencias de **compilación** y las de **invocación** apunten en
sentidos opuestos.

```mermaid
flowchart TB
    subgraph COMP["Dependencias de compilación (POM e imports)"]
        direction TB
        B1["rm-business"] --> D1["rm-domain<br/>ResourceRequestRepository"]
        T1["rm-data"] --> D1
        W1["rm-web"] --> B1
        W1 --> D1
    end

    subgraph RUN["Invocación en ejecución (cableado del contenedor)"]
        direction TB
        W2["ResourceRequestResource"] -->|"@EJB vista @Local"| B2["ResourceRequestServiceBean"]
        B2 -->|"@EJB sobre la interfaz"| T2["JpaResourceRequestRepository"]
        T2 --> DB[("H2")]
    end
```

`rm-business` no declara a `rm-data` en su POM y no importa sus clases. La
implementación se resuelve al hacer deploy, porque `JpaResourceRequestRepository` publica la vista
`@Local(ResourceRequestRepository.class)`.

## Qué hace cada elemento

| Pieza | Responsabilidad | Lo que nunca hace |
|---|---|---|
| `ResourceRequestResource` | Deserializa el JSON y delega | Validar reglas de negocio |
| `ResourceRequestServiceBean` | Valida, coordina y abre la transacción | Conocer JPA, `EntityManager` o JPQL |
| `ResourceRequest` | Protege sus invariantes y su máquina de estados | Consultar la base de datos |
| `ResourceRequestRepository` | Declara lo que el negocio necesita | Decidir qué es un error |
| `JpaResourceRequestRepository` | `persist`, `find`, JPQL | Validar, lanzar excepciones de negocio |


## Edge transaccional

El negocio declara `@TransactionAttribute(REQUIRED)` y el tier de datos `MANDATORY`. El repositorio
nunca abre una transacción propia, así que cualquier intento de usarlo fuera de una operación de
negocio falla de inmediato. La frontera queda donde corresponde y además queda escrita en el código.

Por eso las consultas del negocio también son `REQUIRED` y no `SUPPORTS`: una lectura que entre por
la vía remota llega sin transacción, y con `SUPPORTS` el repositorio la rechazaría.
