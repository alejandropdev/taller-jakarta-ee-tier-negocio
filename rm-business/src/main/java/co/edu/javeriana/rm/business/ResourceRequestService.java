package co.edu.javeriana.rm.business;

import jakarta.ejb.Local;

/**
 * Clientes en la misma JVM, como la fachada REST. Paso por referencia.
 */
@Local
public interface ResourceRequestService extends ResourceRequestOperations {
}
