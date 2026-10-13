# 04 – Secuencia: atender una solicitud

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente remoto
    participant S as ResourceRequestServiceBean<br/>(vista @Remote)
    participant P as JpaResourceRequestRepository
    participant E as ResourceRequest
    participant DB as H2

    C->>S: fulfill(id) por JNDI
    Note over S: Transacción JTA abierta por el negocio
    S->>P: findById(id)
    P->>DB: SELECT
    P-->>S: Optional<ResourceRequest>
    alt No existe
        S--xC: ResourceRequestNotFoundException (404 por REST)
    else Ya está ATENDIDA
        S->>E: fulfill()
        E--xS: RequestAlreadyFulfilledException
        S--xC: excepción de negocio (409 por REST)
        Note over P: No se guarda nada y la transacción revierte
    else Pendiente
        S->>E: fulfill()
        E-->>S: estado ATENDIDA + fecha de atención
        S->>P: save(solicitud)
        P->>DB: UPDATE
        S-->>C: ResourceRequestDTO con status ATENDIDA
    end
```

Leer, validar la transición y escribir ocurren dentro de la misma transacción, de modo que dos
intentos simultáneos de atender la misma solicitud no pueden dejarla en un estado inconsistente.
