package co.edu.javeriana.rm.domain.model;

import co.edu.javeriana.rm.domain.exception.InvalidRequestDataException;
import co.edu.javeriana.rm.domain.exception.RequestAlreadyFulfilledException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Solicitud de recursos de un centro de atención durante una emergencia.
 *
 * Sin constructor público ni setters: la solicitud solo se crea con register y solo cambia de
 * estado con fulfill, así que ninguna capa puede saltarse esas validaciones.
 */
@Entity
@Table(name = "resource_request")
public class ResourceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disaster_description", nullable = false, length = 500)
    private String disasterDescription;

    @Column(name = "resource_type", nullable = false, length = 120)
    private String resourceType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ResourceRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "fulfilled_at")
    private Instant fulfilledAt;

    protected ResourceRequest() {
        // requerido por JPA
    }

    public static ResourceRequest register(String disasterDescription, String resourceType,
                                           int quantity, Priority priority) {
        ResourceRequest request = new ResourceRequest();
        request.disasterDescription = requireText(disasterDescription, "La descripción del desastre es obligatoria");
        request.resourceType = requireText(resourceType, "El tipo de recurso es obligatorio");
        request.quantity = requirePositive(quantity);
        request.priority = requirePriority(priority);
        request.status = ResourceRequestStatus.PENDIENTE;
        request.createdAt = Instant.now();
        return request;
    }

    public void fulfill() {
        if (!isPending()) {
            throw new RequestAlreadyFulfilledException(id);
        }
        status = ResourceRequestStatus.ATENDIDA;
        fulfilledAt = Instant.now();
    }

    public boolean isPending() {
        return status == ResourceRequestStatus.PENDIENTE;
    }

    private static String requireText(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new InvalidRequestDataException(mensaje);
        }
        return valor.trim();
    }

    private static int requirePositive(int cantidad) {
        if (cantidad <= 0) {
            throw new InvalidRequestDataException("La cantidad de recursos solicitada debe ser mayor que cero");
        }
        return cantidad;
    }

    private static Priority requirePriority(Priority prioridad) {
        if (prioridad == null) {
            throw new InvalidRequestDataException("El nivel de prioridad es obligatorio");
        }
        return prioridad;
    }

    public Long getId() { return id; }
    public String getDisasterDescription() { return disasterDescription; }
    public String getResourceType() { return resourceType; }
    public int getQuantity() { return quantity; }
    public Priority getPriority() { return priority; }
    public ResourceRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getFulfilledAt() { return fulfilledAt; }
}
