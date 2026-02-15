package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.command.CreateFranchiseCommand;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CreateFranchiseCommandTest {

    @Test
    void executeShouldTrimValidateCheckExistsSaveAndEmitSavedFranchiseAndAudit() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        CreateFranchiseCommand command = new CreateFranchiseCommand(franchiseRepository, audit);

        String raw = "   Franquicia A   ";
        String trimmed = "Franquicia A";

        when(franchiseRepository.existsByName(trimmed)).thenReturn(Mono.just(false));
        when(franchiseRepository.save(any(Franchise.class)))
                .thenAnswer(inv -> {
                    Franchise f = inv.getArgument(0);
                    return Mono.just(new Franchise("id-1", f.name()));
                });

        StepVerifier.create(command.execute(raw))
                .assertNext(saved -> {
                    assertEquals("id-1", saved.id());
                    assertEquals(trimmed, saved.name());
                })
                .verifyComplete();

        InOrder repoOrder = inOrder(franchiseRepository);
        repoOrder.verify(franchiseRepository).existsByName(trimmed);
        repoOrder.verify(franchiseRepository).save(any(Franchise.class));
        repoOrder.verifyNoMoreInteractions();

        ArgumentCaptor<Franchise> captor = ArgumentCaptor.forClass(Franchise.class);
        verify(franchiseRepository).save(captor.capture());
        Franchise toSave = captor.getValue();
        assertNull(toSave.id());
        assertEquals(trimmed, toSave.name());

        InOrder auditOrder = inOrder(auditLogger);
        auditOrder.verify(auditLogger).info("AUDIT createFranchise name=" + trimmed);
        auditOrder.verify(auditLogger).info("onNext createFranchise");
        auditOrder.verify(auditLogger).info("onComplete createFranchise");
        auditOrder.verifyNoMoreInteractions();
    }

    @Test
    void executeShouldFailValidationWhenNameBlankAndNotCallRepoAndAuditOnError() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        CreateFranchiseCommand command = new CreateFranchiseCommand(franchiseRepository, audit);

        StepVerifier.create(command.execute("   "))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    assertEquals(Messages.FRANCHISE_NAME_REQUIRED, err.getMessage());
                })
                .verify();

        verifyNoInteractions(franchiseRepository);

        verify(auditLogger).error(eq("onError createFranchise"), any(ValidationException.class));
        verify(auditLogger, never()).info(anyString());
        verifyNoMoreInteractions(auditLogger);
    }

    @Test
    void executeShouldFailWithConflictWhenNameAlreadyExistsAndNotSaveAndAuditOnError() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        CreateFranchiseCommand command = new CreateFranchiseCommand(franchiseRepository, audit);

        String raw = "  Franquicia X ";
        String trimmed = "Franquicia X";

        when(franchiseRepository.existsByName(trimmed)).thenReturn(Mono.just(true));

        StepVerifier.create(command.execute(raw))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ConflictException);
                    assertEquals(Messages.FRANCHISE_NAME_EXISTS, err.getMessage());
                })
                .verify();

        verify(franchiseRepository).existsByName(trimmed);
        verify(franchiseRepository, never()).save(any());
        verifyNoMoreInteractions(franchiseRepository);

        verify(auditLogger).error(eq("onError createFranchise"), any(ConflictException.class));
        verify(auditLogger, never()).info(anyString());
        verifyNoMoreInteractions(auditLogger);
    }
}
