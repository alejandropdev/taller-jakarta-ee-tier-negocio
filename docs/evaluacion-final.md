# Evaluación final – cumplimiento de los requisitos

Verificación de cada requisito del *Taller No.2 – Jakarta EE + Tier de Negocio* contra la
implementación y las pruebas.

**Última ejecución (13 de octubre de 2026):** 24 pruebas unitarias en verde (`mvn test`) y
28/28 comprobaciones de extremo a extremo (`scripts/pruebas-e2e.sh`), incluida la invocación
remota del EJB por JNDI.

## 1. Operaciones

| Req. | Requisito | Implementación | Evidencia |
|------|-----------|----------------|-----------|
| RF-1 | Registrar una solicitud con descripción, tipo de recurso, cantidad, prioridad y estado inicial | `ResourceRequestServiceBean.register` → `ResourceRequest.register` | `registerPersistsRequestWithPendingStatus`, `registerReturnsDtoWithSubmittedData`, E2E «RF-1» |
| RF-2 | Consultar todas las solicitudes y buscar por identificador | `findAll` y `findById` | `findAllReturnsEveryRegisteredRequest`, `findByIdReturnsRequestWhenItExists`, E2E «RF-2» |
| RF-3 | Atender una solicitud: PENDIENTE → ATENDIDA | `fulfill` → `ResourceRequest.fulfill` | `fulfillChangesStatusFromPendingToFulfilled`, E2E «RF-3» |
| RF-4 | Consultar únicamente las no atendidas | `findPending` → `findByStatus(PENDIENTE)` | `findPendingOnlyReturnsPendingRequests`, E2E «RF-4» |

## 2. Reglas de negocio

| Req. | Regla | Cómo se cumple | Evidencia |
|------|-------|----------------|-----------|
| RN-1 | Cantidad mayor que cero | `requireQuantity` en el EJB y `requirePositive` en la entidad. La cantidad llega como `Integer`, así que su ausencia también se rechaza | `registerRejectsZeroQuantity`, `registerRejectsNegativeQuantity`, `registerRejectsNullQuantity`, E2E (400) |
| RN-2 | Descripción del desastre y tipo de recurso obligatorios | `requireText` rechaza nulo y cadena en blanco | `registerRejectsNullDisasterDescription`, `registerRejectsBlankDisasterDescription`, `registerRejectsNullResourceType`, `registerRejectsBlankResourceType`, E2E (400) |
| RN-3 | Toda solicitud nueva inicia en PENDIENTE | `ResourceRequest.register` fija el estado. No hay constructor público, setter, ni campo de estado en el cuerpo de la petición | `registerStartsInPendingStatus`, E2E «el estado enviado por el cliente se ignora» |
| RN-4 | No se puede atender una solicitud inexistente | El repositorio devuelve `Optional` vacío y el negocio lo convierte en `ResourceRequestNotFoundException` | `fulfillFailsWhenRequestDoesNotExist`, E2E (404) |
| RN-5 | No se puede atender una ya ATENDIDA | `ResourceRequest.fulfill` verifica `isPending()` | `fulfillFailsWhenRequestIsAlreadyFulfilled`, `fulfillDoesNotSaveWhenRequestIsAlreadyFulfilled`, E2E (409) |
| RN-6 | Las validaciones van en el tier de negocio | El recurso REST solo deserializa y delega; el repositorio JPA no tiene una sola condición de negocio | `registerDoesNotTouchRepositoryWhenValidationFails` (`verifyNoInteractions`), E2E con el cliente remoto, que obtiene los mismos rechazos sin pasar por REST |

## 3. Requisitos técnicos

| Req. | Requisito | Cómo se cumple | Evidencia |
|------|-----------|----------------|-----------|
| RT-1 | Implementar el tier de negocio con Jakarta EE | Jakarta EE 10 sobre WildFly 37, EAR con módulos EJB y web | Despliegue en los logs del contenedor |
| RT-2 | Usar EJB de sesión para encapsular las reglas | `ResourceRequestServiceBean` es `@Stateless` y expone `@Local` y `@Remote`. Justificación del tipo en [architecture/README.md](architecture/README.md#tipo-de-ejb) | `java:global/resource-management/rm-business/ResourceRequestServiceBean!...` en el log de despliegue |
| RT-3 | Mantener el negocio desacoplado de la persistencia | Contrato `ResourceRequestRepository` en el dominio, implementado por `JpaResourceRequestRepository` y cableado por el contenedor. `rm-business` no depende de `rm-data` | [02-desacople-negocio-datos.md](architecture/02-desacople-negocio-datos.md), y las 21 pruebas del bean que corren sin servidor ni base de datos |

## 4. Entregables

| Entregable | Dónde |
|------------|-------|
| Código fuente | Seis módulos Maven en la raíz del proyecto |
| Diseño de la arquitectura | `docs/architecture/` en Markdown con Mermaid, y los mismos documentos en `docs/architecture/pdf/` |
| Explicación del desacople | `docs/architecture/02-desacople-negocio-datos.md` |
| Evidencias de funcionamiento | Salida de `mvn test`, de `scripts/pruebas-e2e.sh` y del cliente remoto |

## 5. Salida de la invocación remota

```
== Invocación remota del tier de negocio ==
Registrada la solicitud 2 en estado PENDIENTE
Solicitudes registradas: 2
Consulta por identificador: Tanques de agua
Pendientes antes de atender: 1
Atendida la solicitud 2, nuevo estado ATENDIDA
Rechazado atender de nuevo: La solicitud 2 ya fue atendida
Rechazada la solicitud inexistente: No existe una solicitud de recursos con identificador 999999
Pendientes después de atender: 0
```

Las reglas se hacen cumplir igual por la vía remota que por REST, porque viven en el EJB y no en
la fachada.
