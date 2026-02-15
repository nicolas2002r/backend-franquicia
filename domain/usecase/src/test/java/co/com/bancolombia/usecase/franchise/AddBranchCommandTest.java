package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.command.AddBranchCommand;
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

class AddBranchCommandTest {

    @Test
    void executeShouldTrimNameValidateFranchiseAndSaveBranchAndAuditOnNextAndOnComplete() {

        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        BranchRepository branchRepository = mock(BranchRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        AddBranchCommand command = new AddBranchCommand(franchiseRepository, branchRepository, audit);

        String franchiseId = "f1";
        String rawName = "   Sucursal Norte   ";
        String trimmed = "Sucursal Norte";

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(new Franchise(franchiseId, "Franquicia A")));

        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(inv -> {
                    Branch b = inv.getArgument(0);
                    return Mono.just(new Branch("b1", b.franchiseId(), b.name()));
                });

        StepVerifier.create(command.execute(franchiseId, rawName))
                .assertNext(saved -> {
                    assertEquals("b1", saved.id());
                    assertEquals(franchiseId, saved.franchiseId());
                    assertEquals(trimmed, saved.name());
                })
                .verifyComplete();

        InOrder repoOrder = inOrder(franchiseRepository, branchRepository);
        repoOrder.verify(franchiseRepository).findById(franchiseId);
        repoOrder.verify(branchRepository).save(any(Branch.class));
        repoOrder.verifyNoMoreInteractions();

        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository).save(captor.capture());
        Branch toSave = captor.getValue();
        assertNull(toSave.id());
        assertEquals(franchiseId, toSave.franchiseId());
        assertEquals(trimmed, toSave.name());

        InOrder auditOrder = inOrder(auditLogger);
        auditOrder.verify(auditLogger).info("onNext addBranch");
        auditOrder.verify(auditLogger).info("onComplete addBranch");
        auditOrder.verifyNoMoreInteractions();
    }

    @Test
    void executeShouldFailValidationWhenNameBlankAndNotCallRepositoriesAndAuditOnError() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        BranchRepository branchRepository = mock(BranchRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        AddBranchCommand command = new AddBranchCommand(franchiseRepository, branchRepository, audit);

        String franchiseId = "f1";
        String blank = "   ";

        StepVerifier.create(command.execute(franchiseId, blank))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    assertEquals(Messages.BRANCH_NAME_REQUIRED, err.getMessage());
                })
                .verify();

        verifyNoInteractions(franchiseRepository);
        verifyNoInteractions(branchRepository);

        verify(auditLogger).error(eq("onError addBranch"), any(ValidationException.class));
        verify(auditLogger, never()).info(anyString());
        verifyNoMoreInteractions(auditLogger);
    }

    @Test
    void executeShouldFailWhenFranchiseNotFoundAndNotSaveBranchAndAuditOnError() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        BranchRepository branchRepository = mock(BranchRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        AddBranchCommand command = new AddBranchCommand(franchiseRepository, branchRepository, audit);

        String franchiseId = "missing";
        String name = "Sucursal";

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(command.execute(franchiseId, name))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.FRANQUICIA_NO_ENCONTRADA + franchiseId, err.getMessage());
                })
                .verify();

        verify(franchiseRepository).findById(franchiseId);
        verifyNoMoreInteractions(franchiseRepository);

        verify(branchRepository, never()).save(any());

        ArgumentCaptor<Throwable> errCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(auditLogger).error(eq("onError addBranch"), errCaptor.capture());
        assertTrue(errCaptor.getValue() instanceof NotFoundException);

        verify(auditLogger, never()).info(anyString());
        verifyNoMoreInteractions(auditLogger);
    }
}

