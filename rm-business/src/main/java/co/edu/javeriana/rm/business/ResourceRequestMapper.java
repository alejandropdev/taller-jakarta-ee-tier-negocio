package co.edu.javeriana.rm.business;

import co.edu.javeriana.rm.domain.dto.ResourceRequestDTO;
import co.edu.javeriana.rm.domain.model.ResourceRequest;
import java.util.List;

/**
 * Traducir a DTO evita que las entidades JPA salgan del tier de negocio.
 */
final class ResourceRequestMapper {

    private ResourceRequestMapper() {
    }

    static ResourceRequestDTO toDto(ResourceRequest entidad) {
        return new ResourceRequestDTO(entidad.getId(), entidad.getDisasterDescription(),
                entidad.getResourceType(), entidad.getQuantity(), entidad.getPriority(),
                entidad.getStatus(), entidad.getCreatedAt(), entidad.getFulfilledAt());
    }

    static List<ResourceRequestDTO> toDtoList(List<ResourceRequest> entidades) {
        return entidades.stream().map(ResourceRequestMapper::toDto).toList();
    }
}
