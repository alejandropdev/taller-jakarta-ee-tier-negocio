package co.edu.javeriana.rm.business;

import jakarta.ejb.Local;

/**
 * Clientes en la misma JVM, como la interfaz REST.
 */
@Local
public interface ResourceRequestService extends ResourceRequestOperations {
}
