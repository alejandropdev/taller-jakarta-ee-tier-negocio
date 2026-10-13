# CLAUDE.md

## Proyecto

**Taller No.2 – Jakarta EE (Tier de Negocio)**, curso *Arquitectura de Software*,
Pontificia Universidad Javeriana (Departamento de Ingeniería de Sistemas).

Módulo de gestión de solicitudes de recursos durante emergencias. Operaciones: registrar una
solicitud, consultarlas todas, consultar por identificador, atender una solicitud y consultar las
pendientes. El tier de negocio se implementa con **EJB de sesión** y se mantiene **desacoplado del
mecanismo de persistencia**.

Stack decidido: **WildFly 37 + Jakarta EE 10 + Java 17 + H2 embebido**, Maven multi-módulo con
empaquetado EAR, Jakarta REST para la fachada y un cliente remoto por JNDI. Diseño en
`docs/architecture/`; `docs/instructions/` es material local y no se versiona.

## Código y ejecución

- Seis módulos Maven: `rm-domain`, `rm-business`, `rm-data`, `rm-web`, `rm-ear`, `rm-client`.
  Paquete raíz `co.edu.javeriana.rm`.
- Levantar: `docker compose up -d --build` (compila y ejecuta las pruebas unitarias).
  API en `http://localhost:8080/api`.
- Pruebas de extremo a extremo: `./scripts/pruebas-e2e.sh` con el stack levantado.
- PDF de la arquitectura: `./scripts/render-docs.sh`.

## Reglas

### Idioma
- **Código** (clases, variables, métodos, paquetes): en **inglés**.
- **Comentarios** en el código: en **español**. Solo declaran secciones o matices que el código no
  dice por sí mismo; nunca repiten lo que ya se lee en la línea siguiente.
- **Texto orientado al usuario** (mensajes de error, respuestas de la API): en **español**.
- **Documentación**: en **español**.
- Los términos de dominio (`PENDIENTE`, `ATENDIDA`) se conservan en español.

### Diseño
- Las reglas de negocio viven en el EJB de sesión y en los invariantes de la entidad, nunca en la
  fachada REST ni en el tier de datos.
- El contrato de persistencia se declara en `rm-domain`; `rm-business` no depende de `rm-data`.
- El tier de negocio abre la transacción (`REQUIRED`); el tier de datos la exige (`MANDATORY`).
- La entidad no tiene setters: se crea por su método de fábrica y cambia de estado por `fulfill()`.

### Flujo de trabajo
- Registrar los supuestos y las decisiones abiertas en `docs/instructions/04-supuestos.md`.
- Mantener `docs/evaluacion-final.md` al día con la evidencia de cada requisito.
