package co.edu.javeriana.rm.business;

import co.edu.javeriana.rm.domain.dto.RegisterResourceRequestCommand;
import co.edu.javeriana.rm.domain.dto.ResourceRequestDTO;
import co.edu.javeriana.rm.domain.exception.InvalidRequestDataException;
import co.edu.javeriana.rm.domain.exception.ResourceRequestNotFoundException;
import co.edu.javeriana.rm.domain.model.ResourceRequest;
import co.edu.javeriana.rm.domain.model.ResourceRequestStatus;
import co.edu.javeriana.rm.domain.repository.ResourceRequestRepository;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.List;

/**
 * Reglas de negocio de la gestión de solicitudes.
 *
 * Sin estado porque cada operación es autocontenida: el contenedor atiende peticiones concurrentes
 * desde su pool y el estado queda en la base de datos.
 *
 * REQUIRED en toda la clase, consultas incluidas, porque este tier es la frontera transaccional y
 * el tier de datos exige una transacción en curso.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ResourceRequestServiceBean implements ResourceRequestService, ResourceRequestServiceRemote {

    /** Inyectado por la interfaz del dominio, de modo que aquí no se conoce la implementación JPA. */
    @EJB
    private ResourceRequestRepository repository;

    @Override
    public ResourceRequestDTO register(RegisterResourceRequestCommand command) {
        if (command == null) {
            throw new InvalidRequestDataException("Los datos de la solicitud son obligatorios");
        }
        int cantidad = requireQuantity(command.quantity());

        ResourceRequest solicitud = ResourceRequest.register(command.disasterDescription(),
                command.resourceType(), cantidad, command.priority());
        return ResourceRequestMapper.toDto(repository.save(solicitud));
    }

    @Override
    public List<ResourceRequestDTO> findAll() {
        return ResourceRequestMapper.toDtoList(repository.findAll());
    }

    @Override
    public ResourceRequestDTO findById(Long id) {
        return ResourceRequestMapper.toDto(find(id));
    }

    @Override
    public ResourceRequestDTO fulfill(Long id) {
        ResourceRequest solicitud = find(id);
        solicitud.fulfill();
        return ResourceRequestMapper.toDto(repository.save(solicitud));
    }

    @Override
    public List<ResourceRequestDTO> findPending() {
        return ResourceRequestMapper.toDtoList(repository.findByStatus(ResourceRequestStatus.PENDIENTE));
    }

    /** El repositorio informa que no hay resultado, decidir que eso es un error corresponde aquí. */
    private ResourceRequest find(Long id) {
        if (id == null) {
            throw new InvalidRequestDataException("El identificador de la solicitud es obligatorio");
        }
        return repository.findById(id).orElseThrow(() -> new ResourceRequestNotFoundException(id));
    }

    private static int requireQuantity(Integer cantidad) {
        if (cantidad == null) {
            throw new InvalidRequestDataException("La cantidad de recursos solicitada es obligatoria");
        }
        if (cantidad <= 0) {
            throw new InvalidRequestDataException("La cantidad de recursos solicitada debe ser mayor que cero");
        }
        return cantidad;
    }
}
