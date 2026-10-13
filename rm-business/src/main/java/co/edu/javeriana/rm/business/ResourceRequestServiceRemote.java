package co.edu.javeriana.rm.business;

import jakarta.ejb.Remote;

/**
 * Clientes en otra JVM que invocan por JNDI. Paso por valor, de ahí que los DTO sean serializables.
 */
@Remote
public interface ResourceRequestServiceRemote extends ResourceRequestOperations {
}
