package co.edu.javeriana.rm.domain.exception;

import jakarta.ejb.ApplicationException;

/**
 * La anotación evita que el contenedor envuelva estas excepciones en EJBException: llegan íntegras
 * al cliente, también por la vía remota, y marcan la transacción para rollback.
 */
@ApplicationException(rollback = true, inherited = true)
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String mensaje) {
        super(mensaje);
    }
}
