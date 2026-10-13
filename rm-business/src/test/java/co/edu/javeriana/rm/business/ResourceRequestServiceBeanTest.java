package co.edu.javeriana.rm.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.javeriana.rm.domain.dto.RegisterResourceRequestCommand;
import co.edu.javeriana.rm.domain.dto.ResourceRequestDTO;
import co.edu.javeriana.rm.domain.exception.InvalidRequestDataException;
import co.edu.javeriana.rm.domain.exception.RequestAlreadyFulfilledException;
import co.edu.javeriana.rm.domain.exception.ResourceRequestNotFoundException;
import co.edu.javeriana.rm.domain.model.Priority;
import co.edu.javeriana.rm.domain.model.ResourceRequest;
import co.edu.javeriana.rm.domain.model.ResourceRequestStatus;
import co.edu.javeriana.rm.domain.repository.ResourceRequestRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Las reglas se verifican con el repositorio simulado, sin servidor de aplicaciones ni base de datos.
 */
@ExtendWith(MockitoExtension.class)
class ResourceRequestServiceBeanTest {

    private static final String DESASTRE = "Deslizamiento de tierra en la vía al llano";
    private static final String RECURSO = "Frazadas";

    @Mock
    private ResourceRequestRepository repository;

    @InjectMocks
    private ResourceRequestServiceBean service;

    private static RegisterResourceRequestCommand comando(String desastre, String recurso,
                                                          Integer cantidad, Priority prioridad) {
        return new RegisterResourceRequestCommand(desastre, recurso, cantidad, prioridad);
    }

    private static ResourceRequest solicitudPendiente() {
        return ResourceRequest.register(DESASTRE, RECURSO, 120, Priority.ALTA);
    }

    // --- Registrar ---

    @Test
    void registerPersistsRequestWithPendingStatus() {
        when(repository.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        ResourceRequestDTO registrada = service.register(comando(DESASTRE, RECURSO, 120, Priority.ALTA));

        ArgumentCaptor<ResourceRequest> capturada = ArgumentCaptor.forClass(ResourceRequest.class);
        verify(repository).save(capturada.capture());
        assertEquals(ResourceRequestStatus.PENDIENTE, capturada.getValue().getStatus());
        assertEquals(ResourceRequestStatus.PENDIENTE, registrada.status());
    }

    @Test
    void registerReturnsDtoWithSubmittedData() {
        when(repository.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        ResourceRequestDTO registrada = service.register(comando(DESASTRE, RECURSO, 120, Priority.ALTA));

        assertEquals(DESASTRE, registrada.disasterDescription());
        assertEquals(RECURSO, registrada.resourceType());
        assertEquals(120, registrada.quantity());
        assertEquals(Priority.ALTA, registrada.priority());
        assertNotNull(registrada.createdAt());
    }

    @Test
    void registerRejectsZeroQuantity() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando(DESASTRE, RECURSO, 0, Priority.ALTA)));
    }

    @Test
    void registerRejectsNegativeQuantity() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando(DESASTRE, RECURSO, -3, Priority.ALTA)));
    }

    @Test
    void registerRejectsNullQuantity() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando(DESASTRE, RECURSO, null, Priority.ALTA)));
    }

    @Test
    void registerRejectsNullDisasterDescription() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando(null, RECURSO, 10, Priority.ALTA)));
    }

    @Test
    void registerRejectsBlankDisasterDescription() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando("   ", RECURSO, 10, Priority.ALTA)));
    }

    @Test
    void registerRejectsNullResourceType() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando(DESASTRE, null, 10, Priority.ALTA)));
    }

    @Test
    void registerRejectsBlankResourceType() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando(DESASTRE, "", 10, Priority.ALTA)));
    }

    @Test
    void registerRejectsNullPriority() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando(DESASTRE, RECURSO, 10, null)));
    }

    @Test
    void registerDoesNotTouchRepositoryWhenValidationFails() {
        assertThrows(InvalidRequestDataException.class,
                () -> service.register(comando(DESASTRE, RECURSO, 0, Priority.ALTA)));

        verifyNoInteractions(repository);
    }

    // --- Consultar ---

    @Test
    void findAllReturnsEveryRegisteredRequest() {
        when(repository.findAll()).thenReturn(List.of(solicitudPendiente(), solicitudPendiente()));

        assertEquals(2, service.findAll().size());
    }

    @Test
    void findAllReturnsEmptyListWhenThereAreNoRequests() {
        when(repository.findAll()).thenReturn(List.of());

        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void findByIdReturnsRequestWhenItExists() {
        when(repository.findById(7L)).thenReturn(Optional.of(solicitudPendiente()));

        assertEquals(RECURSO, service.findById(7L).resourceType());
    }

    @Test
    void findByIdFailsWhenRequestDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceRequestNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void findPendingOnlyReturnsPendingRequests() {
        when(repository.findByStatus(ResourceRequestStatus.PENDIENTE))
                .thenReturn(List.of(solicitudPendiente()));

        assertEquals(1, service.findPending().size());
        verify(repository).findByStatus(ResourceRequestStatus.PENDIENTE);
    }

    // --- Atender ---

    @Test
    void fulfillChangesStatusFromPendingToFulfilled() {
        when(repository.findById(5L)).thenReturn(Optional.of(solicitudPendiente()));
        when(repository.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals(ResourceRequestStatus.ATENDIDA, service.fulfill(5L).status());
    }

    @Test
    void fulfillRecordsFulfillmentTimestamp() {
        when(repository.findById(5L)).thenReturn(Optional.of(solicitudPendiente()));
        when(repository.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertNotNull(service.fulfill(5L).fulfilledAt());
    }

    @Test
    void fulfillFailsWhenRequestDoesNotExist() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceRequestNotFoundException.class, () -> service.fulfill(404L));
    }

    @Test
    void fulfillFailsWhenRequestIsAlreadyFulfilled() {
        ResourceRequest atendida = solicitudPendiente();
        atendida.fulfill();
        when(repository.findById(5L)).thenReturn(Optional.of(atendida));

        assertThrows(RequestAlreadyFulfilledException.class, () -> service.fulfill(5L));
    }

    @Test
    void fulfillDoesNotSaveWhenRequestIsAlreadyFulfilled() {
        ResourceRequest atendida = solicitudPendiente();
        atendida.fulfill();
        when(repository.findById(5L)).thenReturn(Optional.of(atendida));

        assertThrows(RequestAlreadyFulfilledException.class, () -> service.fulfill(5L));
        verify(repository, never()).save(any());
    }
}
