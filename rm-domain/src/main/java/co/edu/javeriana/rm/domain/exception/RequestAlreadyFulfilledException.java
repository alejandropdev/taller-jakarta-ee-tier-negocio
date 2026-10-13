package co.edu.javeriana.rm.domain.exception;

public class RequestAlreadyFulfilledException extends BusinessException {

    public RequestAlreadyFulfilledException(Long id) {
        super("La solicitud " + id + " ya fue atendida");
    }
}
