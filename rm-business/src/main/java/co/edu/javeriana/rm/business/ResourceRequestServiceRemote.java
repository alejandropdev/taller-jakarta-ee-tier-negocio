package co.edu.javeriana.rm.business;

import jakarta.ejb.Remote;

/**
 * Clientes en otra JVM que invocan por JNDI.
 */
@Remote
public interface ResourceRequestServiceRemote extends ResourceRequestOperations {
}
