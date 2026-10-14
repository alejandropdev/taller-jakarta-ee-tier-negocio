package co.edu.javeriana.rm.web;

import co.edu.javeriana.rm.domain.exception.BusinessException;
import co.edu.javeriana.rm.domain.exception.InvalidRequestDataException;
import co.edu.javeriana.rm.domain.exception.RequestAlreadyFulfilledException;
import co.edu.javeriana.rm.domain.exception.ResourceRequestNotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traduce las excepciones de negocio a códigos HTTP. Recorre la cadena de causas porque el
 * contenedor puede envolver lo que cruza la frontera del EJB.
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable excepcion) {
        if (buscar(excepcion, ResourceRequestNotFoundException.class) != null) {
            return responder(Response.Status.NOT_FOUND, buscar(excepcion, BusinessException.class));
        }
        if (buscar(excepcion, RequestAlreadyFulfilledException.class) != null) {
            return responder(Response.Status.CONFLICT, buscar(excepcion, BusinessException.class));
        }
        if (buscar(excepcion, InvalidRequestDataException.class) != null) {
            return responder(Response.Status.BAD_REQUEST, buscar(excepcion, BusinessException.class));
        }
        WebApplicationException web = buscar(excepcion, WebApplicationException.class);
        if (web != null) {
            Response.Status estado = Response.Status.fromStatusCode(web.getResponse().getStatus());
            return construir(estado, web.getMessage());
        }
        LOG.log(Level.SEVERE, "Error no controlado en la API", excepcion);
        return construir(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private Response responder(Response.Status estado, BusinessException negocio) {
        return construir(estado, negocio.getMessage());
    }

    private Response construir(Response.Status estado, String mensaje) {
        return Response.status(estado)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(estado.getStatusCode(), estado.getReasonPhrase(), mensaje))
                .build();
    }

    private static <T extends Throwable> T buscar(Throwable excepcion, Class<T> tipo) {
        for (Throwable actual = excepcion; actual != null; actual = actual.getCause()) {
            if (tipo.isInstance(actual)) {
                return tipo.cast(actual);
            }
            if (actual.getCause() == actual) {
                break;
            }
        }
        return null;
    }
}
