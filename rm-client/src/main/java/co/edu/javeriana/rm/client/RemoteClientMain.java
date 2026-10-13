package co.edu.javeriana.rm.client;

import co.edu.javeriana.rm.business.ResourceRequestServiceRemote;
import co.edu.javeriana.rm.domain.dto.RegisterResourceRequestCommand;
import co.edu.javeriana.rm.domain.dto.ResourceRequestDTO;
import co.edu.javeriana.rm.domain.exception.RequestAlreadyFulfilledException;
import co.edu.javeriana.rm.domain.exception.ResourceRequestNotFoundException;
import co.edu.javeriana.rm.domain.model.Priority;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Invoca el tier de negocio desde otra JVM, por la vista remota del EJB y sin pasar por HTTP.
 */
public final class RemoteClientMain {

    /** Formato: ejb:aplicación/módulo/bean!interfaz remota */
    private static final String JNDI_NAME =
            "ejb:resource-management/rm-business/ResourceRequestServiceBean"
            + "!co.edu.javeriana.rm.business.ResourceRequestServiceRemote";

    private static final String PROVIDER_URL = System.getProperty("rm.provider.url", "http-remoting://localhost:8080");
    private static final String USER = System.getProperty("rm.user", "rmclient");
    private static final String PASSWORD = System.getProperty("rm.password", "Rm.client1!");

    private RemoteClientMain() {
    }

    public static void main(String[] args) throws Exception {
        ResourceRequestServiceRemote servicio = lookup();

        System.out.println("== Invocación remota del tier de negocio ==");

        ResourceRequestDTO registrada = servicio.register(new RegisterResourceRequestCommand(
                "Incendio forestal en la vereda alta", "Tanques de agua", 15, Priority.ALTA));
        System.out.printf("Registrada la solicitud %d en estado %s%n", registrada.id(), registrada.status());

        System.out.printf("Solicitudes registradas: %d%n", servicio.findAll().size());
        System.out.printf("Consulta por identificador: %s%n", servicio.findById(registrada.id()).resourceType());
        System.out.printf("Pendientes antes de atender: %d%n", servicio.findPending().size());

        System.out.printf("Atendida la solicitud %d, nuevo estado %s%n",
                registrada.id(), servicio.fulfill(registrada.id()).status());

        // Las reglas se hacen cumplir igual por la vía remota que por REST
        try {
            servicio.fulfill(registrada.id());
            System.out.println("ERROR: se permitió atender dos veces la misma solicitud");
        } catch (RequestAlreadyFulfilledException esperada) {
            System.out.println("Rechazado atender de nuevo: " + esperada.getMessage());
        }

        try {
            servicio.findById(999999L);
            System.out.println("ERROR: se permitió consultar una solicitud inexistente");
        } catch (ResourceRequestNotFoundException esperada) {
            System.out.println("Rechazada la solicitud inexistente: " + esperada.getMessage());
        }

        System.out.printf("Pendientes después de atender: %d%n", servicio.findPending().size());
    }

    private static ResourceRequestServiceRemote lookup() throws Exception {
        Properties jndiProperties = new Properties();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.PROVIDER_URL, PROVIDER_URL);
        jndiProperties.put(Context.SECURITY_PRINCIPAL, USER);
        jndiProperties.put(Context.SECURITY_CREDENTIALS, PASSWORD);
        jndiProperties.put("jboss.naming.client.ejb.context", true);

        Context contexto = new InitialContext(jndiProperties);
        return (ResourceRequestServiceRemote) contexto.lookup(JNDI_NAME);
    }
}
