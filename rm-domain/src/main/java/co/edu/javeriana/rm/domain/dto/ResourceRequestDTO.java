package co.edu.javeriana.rm.domain.dto;

import co.edu.javeriana.rm.domain.model.Priority;
import co.edu.javeriana.rm.domain.model.ResourceRequestStatus;
import java.io.Serializable;
import java.time.Instant;

/**
 * Serializable porque viaja por la vista remota del EJB.
 */
public record ResourceRequestDTO(Long id, String disasterDescription, String resourceType,
                                 int quantity, Priority priority, ResourceRequestStatus status,
                                 Instant createdAt, Instant fulfilledAt) implements Serializable {
}
