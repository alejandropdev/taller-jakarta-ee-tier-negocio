package co.edu.javeriana.rm.domain.exception;

public class ResourceRequestNotFoundException extends BusinessException {

    public ResourceRequestNotFoundException(Long id) {
        super("No existe una solicitud de recursos con identificador " + id);
    }
}
