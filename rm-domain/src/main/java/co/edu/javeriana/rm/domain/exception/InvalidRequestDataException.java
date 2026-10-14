package co.edu.javeriana.rm.domain.exception;

public class InvalidRequestDataException extends BusinessException {

    public InvalidRequestDataException(String mensaje) {
        super(mensaje);
    }
}
