package co.edu.javeriana.rm.domain.dto;

import co.edu.javeriana.rm.domain.model.Priority;
import java.io.Serializable;

/**
 * quantity es Integer y no int para que la ausencia del dato llegue como null al tier de negocio
 * y sea este quien la rechace.
 */
public record RegisterResourceRequestCommand(String disasterDescription, String resourceType,
                                             Integer quantity, Priority priority) implements Serializable {
}
