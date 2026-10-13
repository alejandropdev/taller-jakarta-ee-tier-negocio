package co.edu.javeriana.rm.web;

import co.edu.javeriana.rm.business.ResourceRequestService;
import co.edu.javeriana.rm.domain.dto.RegisterResourceRequestCommand;
import co.edu.javeriana.rm.domain.dto.ResourceRequestDTO;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import java.util.List;

/**
 * Fachada HTTP: deserializa, delega en el EJB por su vista local y deja la traducción de errores
 * al mapper. No decide nada sobre las reglas de negocio.
 */
@Path("/resource-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResourceRequestResource {

    @EJB
    private ResourceRequestService service;

    @Context
    private UriInfo uriInfo;

    @POST
    public Response create(RegisterResourceRequestBody cuerpo) {
        RegisterResourceRequestCommand comando = cuerpo == null ? null
                : new RegisterResourceRequestCommand(cuerpo.disasterDescription(), cuerpo.resourceType(),
                        cuerpo.quantity(), cuerpo.priority());
        ResourceRequestDTO registrada = service.register(comando);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(registrada.id())).build())
                .entity(registrada)
                .build();
    }

    @GET
    public List<ResourceRequestDTO> listAll() {
        return service.findAll();
    }

    @GET
    @Path("/pending")
    public List<ResourceRequestDTO> listPending() {
        return service.findPending();
    }

    @GET
    @Path("/{id}")
    public ResourceRequestDTO findById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @Path("/{id}/fulfillment")
    public ResourceRequestDTO fulfill(@PathParam("id") Long id) {
        return service.fulfill(id);
    }
}
