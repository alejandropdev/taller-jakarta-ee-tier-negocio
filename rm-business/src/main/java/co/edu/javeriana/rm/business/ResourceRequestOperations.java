package co.edu.javeriana.rm.business;

import co.edu.javeriana.rm.domain.dto.RegisterResourceRequestCommand;
import co.edu.javeriana.rm.domain.dto.ResourceRequestDTO;
import java.util.List;

public interface ResourceRequestOperations {

    ResourceRequestDTO register(RegisterResourceRequestCommand command);

    List<ResourceRequestDTO> findAll();

    ResourceRequestDTO findById(Long id);

    ResourceRequestDTO fulfill(Long id);

    List<ResourceRequestDTO> findPending();
}
