package co.edu.javeriana.rm.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.javeriana.rm.domain.exception.InvalidRequestDataException;
import co.edu.javeriana.rm.domain.exception.RequestAlreadyFulfilledException;
import org.junit.jupiter.api.Test;

class ResourceRequestTest {

    private static final String DESASTRE = "Inundación en la ribera del río";
    private static final String RECURSO = "Agua potable";

    @Test
    void registerStartsInPendingStatus() {
        ResourceRequest solicitud = ResourceRequest.register(DESASTRE, RECURSO, 50, Priority.ALTA);

        assertEquals(ResourceRequestStatus.PENDIENTE, solicitud.getStatus());
        assertTrue(solicitud.isPending());
        assertNotNull(solicitud.getCreatedAt());
    }

    @Test
    void fulfillTwiceThrowsRequestAlreadyFulfilled() {
        ResourceRequest solicitud = ResourceRequest.register(DESASTRE, RECURSO, 50, Priority.ALTA);
        solicitud.fulfill();

        assertFalse(solicitud.isPending());
        assertNotNull(solicitud.getFulfilledAt());
        assertThrows(RequestAlreadyFulfilledException.class, solicitud::fulfill);
    }

    @Test
    void registerWithNonPositiveQuantityThrowsInvalidRequestData() {
        assertThrows(InvalidRequestDataException.class,
                () -> ResourceRequest.register(DESASTRE, RECURSO, 0, Priority.MEDIA));
    }
}
