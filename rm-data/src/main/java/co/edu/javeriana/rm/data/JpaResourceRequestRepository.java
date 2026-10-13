package co.edu.javeriana.rm.data;

import co.edu.javeriana.rm.domain.model.ResourceRequest;
import co.edu.javeriana.rm.domain.model.ResourceRequestStatus;
import co.edu.javeriana.rm.domain.repository.ResourceRequestRepository;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

/**
 * MANDATORY por decisión de diseño: este tier nunca abre una transacción propia, así que usarlo
 * fuera de una operación de negocio falla de inmediato.
 */
@Stateless
@Local(ResourceRequestRepository.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class JpaResourceRequestRepository implements ResourceRequestRepository {

    @PersistenceContext(unitName = "resourceRequestPU")
    private EntityManager entityManager;

    @Override
    public ResourceRequest save(ResourceRequest request) {
        if (request.getId() == null) {
            entityManager.persist(request);
            return request;
        }
        return entityManager.merge(request);
    }

    @Override
    public Optional<ResourceRequest> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ResourceRequest.class, id));
    }

    @Override
    public List<ResourceRequest> findAll() {
        return entityManager.createQuery(
                        "SELECT r FROM ResourceRequest r ORDER BY r.id", ResourceRequest.class)
                .getResultList();
    }

    @Override
    public List<ResourceRequest> findByStatus(ResourceRequestStatus status) {
        return entityManager.createQuery(
                        "SELECT r FROM ResourceRequest r WHERE r.status = :estado ORDER BY r.id",
                        ResourceRequest.class)
                .setParameter("estado", status)
                .getResultList();
    }
}
