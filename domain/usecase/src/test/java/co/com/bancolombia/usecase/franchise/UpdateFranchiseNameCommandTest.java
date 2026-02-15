package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.command.UpdateFranchiseNameCommand;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UpdateFranchiseNameCommandTest {

    private static class AuditCounters {
        final AtomicInteger onNext = new AtomicInteger(0);
        final AtomicInteger onComplete = new AtomicInteger(0);
        final AtomicInteger onError = new AtomicInteger(0);
    }

    private static AuditSupport auditSupportWithCounters(AuditLogger auditLogger, AuditCounters c, String op) {
        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext " + op)) return Mono.fromRunnable(c.onNext::incrementAndGet);
            if (msg.equals("onComplete " + op)) return Mono.fromRunnable(c.onComplete::incrementAndGet);
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(c.onError::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        return new AuditSupport(auditLogger);
    }

    @Test
    void execute_shouldUpdateName_whenValid_andLogOnNextOnce_andOnCompleteOnce() {
        FranchiseRepository repo = mock(FranchiseRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateFranchiseName");
        UpdateFranchiseNameCommand cmd = new UpdateFranchiseNameCommand(repo, audit);

        String franchiseId = "f1";
        when(repo.existsByName("Nuevo")).thenReturn(Mono.just(false));
        when(repo.updateName(franchiseId, "Nuevo"))
                .thenReturn(Mono.just(new Franchise(franchiseId, "Nuevo")));

        StepVerifier.create(cmd.execute(franchiseId, "  Nuevo  "))
                .assertNext(f -> {
                    assertEquals(franchiseId, f.id());
                    assertEquals("Nuevo", f.name());
                })
                .verifyComplete();

        verify(repo).existsByName("Nuevo");
        verify(repo).updateName(franchiseId, "Nuevo");
        verifyNoMoreInteractions(repo);

        assertEquals(1, c.onNext.get());
        assertEquals(1, c.onComplete.get());
        assertEquals(0, c.onError.get());
    }

    @Test
    void execute_shouldFail_whenNameBlank_andNotTouchRepo_andLogOnError() {
        FranchiseRepository repo = mock(FranchiseRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateFranchiseName");
        UpdateFranchiseNameCommand cmd = new UpdateFranchiseNameCommand(repo, audit);

        StepVerifier.create(cmd.execute("f1", "   "))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    assertEquals(Messages.FRANCHISE_NAME_REQUIRED, err.getMessage());
                })
                .verify();

        verifyNoInteractions(repo);

        assertEquals(0, c.onNext.get());
        assertEquals(0, c.onComplete.get());
        assertEquals(1, c.onError.get());
    }

    @Test
    void execute_shouldFail_withConflict_whenNameAlreadyExists_andNotCallUpdate_andLogOnError() {
        FranchiseRepository repo = mock(FranchiseRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateFranchiseName");
        UpdateFranchiseNameCommand cmd = new UpdateFranchiseNameCommand(repo, audit);

        when(repo.existsByName("X")).thenReturn(Mono.just(true));

        StepVerifier.create(cmd.execute("f1", "X"))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ConflictException);
                    assertEquals(Messages.FRANCHISE_NAME_EXISTS, err.getMessage());
                })
                .verify();

        verify(repo).existsByName("X");
        verify(repo, never()).updateName(anyString(), anyString());
        verifyNoMoreInteractions(repo);

        assertEquals(0, c.onNext.get());
        assertEquals(0, c.onComplete.get());
        assertEquals(1, c.onError.get());
    }

    @Test
    void execute_shouldFail_withNotFound_whenUpdateReturnsEmpty_andLogOnError() {
        FranchiseRepository repo = mock(FranchiseRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateFranchiseName");
        UpdateFranchiseNameCommand cmd = new UpdateFranchiseNameCommand(repo, audit);

        String franchiseId = "f404";
        when(repo.existsByName("Nuevo")).thenReturn(Mono.just(false));
        when(repo.updateName(franchiseId, "Nuevo")).thenReturn(Mono.empty());

        StepVerifier.create(cmd.execute(franchiseId, "Nuevo"))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.FRANQUICIA_NO_ENCONTRADA + franchiseId, err.getMessage());
                })
                .verify();

        verify(repo).existsByName("Nuevo");
        verify(repo).updateName(franchiseId, "Nuevo");
        verifyNoMoreInteractions(repo);

        assertEquals(0, c.onNext.get());
        assertEquals(0, c.onComplete.get());
        assertEquals(1, c.onError.get());
    }

    @Test
    void execute_shouldPropagateRepoError_andLogOnError() {
        FranchiseRepository repo = mock(FranchiseRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateFranchiseName");
        UpdateFranchiseNameCommand cmd = new UpdateFranchiseNameCommand(repo, audit);

        RuntimeException boom = new RuntimeException("db down");
        when(repo.existsByName("Nuevo")).thenReturn(Mono.just(false));
        when(repo.updateName("f1", "Nuevo")).thenReturn(Mono.error(boom));

        StepVerifier.create(cmd.execute("f1", "Nuevo"))
                .expectErrorSatisfies(err -> assertSame(boom, err))
                .verify();

        verify(repo).existsByName("Nuevo");
        verify(repo).updateName("f1", "Nuevo");
        verifyNoMoreInteractions(repo);

        assertEquals(0, c.onNext.get());
        assertEquals(0, c.onComplete.get());
        assertEquals(1, c.onError.get());
    }
}

