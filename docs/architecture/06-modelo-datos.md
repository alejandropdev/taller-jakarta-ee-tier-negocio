# 06 – Modelo de datos

```mermaid
erDiagram
    RESOURCE_REQUEST {
        bigint id PK "IDENTITY"
        varchar disaster_description "obligatorio, 500"
        varchar resource_type "obligatorio, 120"
        int quantity "mayor que cero"
        varchar priority "BAJA · MEDIA · ALTA"
        varchar status "PENDIENTE · ATENDIDA"
        timestamp created_at "obligatorio"
        timestamp fulfilled_at "nulo mientras está pendiente"
    }
```

## Unidad de persistencia

| Aspecto | Valor |
|---|---|
| Nombre | `resourceRequestPU` |
| Tipo de transacción | `JTA` |
| Datasource | `java:jboss/datasources/ResourceManagementDS` |
| Base de datos | H2 en memoria, embebida en WildFly |
| Generación del esquema | `hibernate.hbm2ddl.auto=create` |

El `persistence.xml` viaja en `lib/rm-domain.jar`, junto al mapeo de la entidad, de modo que la
unidad tiene alcance de toda la aplicación. El tier de negocio nunca lo referencia.
