package co.edu.javeriana.rm.domain.repository;

import co.edu.javeriana.rm.domain.model.ResourceRequest;
import co.edu.javeriana.rm.domain.model.ResourceRequestStatus;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia declarado en el dominio y no en el tier de datos: así la dependencia de
 * compilación va de datos hacia el dominio, y cambiar de mecanismo de persistencia no toca al
 * tier de negocio.
 */
public interface ResourceRequestRepository {

    ResourceRequest save(ResourceRequest request);

    Optional<ResourceRequest> findById(Long id);

    List<ResourceRequest> findAll();

    List<ResourceRequest> findByStatus(ResourceRequestStatus status);
}
