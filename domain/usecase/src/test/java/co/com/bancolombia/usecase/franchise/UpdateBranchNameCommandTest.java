package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.usecase.franchise.command.UpdateBranchNameCommand;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UpdateBranchNameCommandTest {

    @Test
    void execute_shouldUpdateName_andLogOnNextOnce_andLogOnCompleteOnce() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onNextSubscribed = new AtomicInteger(0);
        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);

        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext updateBranchName")) {
                return Mono.fromRunnable(onNextSubscribed::incrementAndGet);
            }
            if (msg.equals("onComplete updateBranchName")) {
                return Mono.fromRunnable(onCompleteSubscribed::incrementAndGet);
            }
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(onErrorSubscribed::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        AuditSupport audit = new AuditSupport(auditLogger);
        UpdateBranchNameCommand command = new UpdateBranchNameCommand(branchRepository, audit);

        String franchiseId = "f1";
        String branchId = "b1";

        when(branchRepository.updateName(branchId, franchiseId, "Sucursal Nueva"))
                .thenReturn(Mono.just(new Branch(branchId, franchiseId, "Sucursal Nueva")));

        StepVerifier.create(command.execute(franchiseId, branchId, "  Sucursal Nueva  "))
                .assertNext(b -> {
                    assertEquals(branchId, b.id());
                    assertEquals(franchiseId, b.franchiseId());
                    assertEquals("Sucursal Nueva", b.name());
                })
                .verifyComplete();

        verify(branchRepository).updateName(branchId, franchiseId, "Sucursal Nueva");
        verifyNoMoreInteractions(branchRepository);

        assertEquals(1, onNextSubscribed.get());
        assertEquals(1, onCompleteSubscribed.get());
        assertEquals(0, onErrorSubscribed.get());
    }

    @Test
    void execute_shouldFail_whenNameIsNull_orBlank_andNotCallRepository_andLogOnError() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onNextSubscribed = new AtomicInteger(0);
        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);

        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext updateBranchName")) return Mono.fromRunnable(onNextSubscribed::incrementAndGet);
            if (msg.equals("onComplete updateBranchName")) return Mono.fromRunnable(onCompleteSubscribed::incrementAndGet);
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(onErrorSubscribed::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        AuditSupport audit = new AuditSupport(auditLogger);
        UpdateBranchNameCommand command = new UpdateBranchNameCommand(branchRepository, audit);

        StepVerifier.create(command.execute("f1", "b1", "   "))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    assertEquals(Messages.BRANCH_NAME_REQUIRED, err.getMessage());
                })
                .verify();

        verifyNoInteractions(branchRepository);

        assertEquals(0, onNextSubscribed.get());
        assertEquals(0, onCompleteSubscribed.get());
        assertEquals(1, onErrorSubscribed.get());
    }

    @Test
    void execute_shouldFail_whenRepositoryReturnsEmpty_andLogOnError() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onNextSubscribed = new AtomicInteger(0);
        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);

        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext updateBranchName")) return Mono.fromRunnable(onNextSubscribed::incrementAndGet);
            if (msg.equals("onComplete updateBranchName")) return Mono.fromRunnable(onCompleteSubscribed::incrementAndGet);
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(onErrorSubscribed::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        AuditSupport audit = new AuditSupport(auditLogger);
        UpdateBranchNameCommand command = new UpdateBranchNameCommand(branchRepository, audit);

        when(branchRepository.updateName("b404", "f1", "New"))
                .thenReturn(Mono.empty());

        StepVerifier.create(command.execute("f1", "b404", "New"))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.NO_SUCURSAL, err.getMessage());
                })
                .verify();

        verify(branchRepository).updateName("b404", "f1", "New");
        verifyNoMoreInteractions(branchRepository);

        assertEquals(0, onNextSubscribed.get());
        assertEquals(0, onCompleteSubscribed.get());
        assertEquals(1, onErrorSubscribed.get());
    }

    @Test
    void execute_shouldPropagateRepositoryError_andLogOnError() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onNextSubscribed = new AtomicInteger(0);
        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);

        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext updateBranchName")) return Mono.fromRunnable(onNextSubscribed::incrementAndGet);
            if (msg.equals("onComplete updateBranchName")) return Mono.fromRunnable(onCompleteSubscribed::incrementAndGet);
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(onErrorSubscribed::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        AuditSupport audit = new AuditSupport(auditLogger);
        UpdateBranchNameCommand command = new UpdateBranchNameCommand(branchRepository, audit);

        RuntimeException boom = new RuntimeException("db down");
        when(branchRepository.updateName("b1", "f1", "New"))
                .thenReturn(Mono.error(boom));

        StepVerifier.create(command.execute("f1", "b1", "New"))
                .expectErrorSatisfies(err -> assertSame(boom, err))
                .verify();

        verify(branchRepository).updateName("b1", "f1", "New");
        verifyNoMoreInteractions(branchRepository);

        assertEquals(0, onNextSubscribed.get());
        assertEquals(0, onCompleteSubscribed.get());
        assertEquals(1, onErrorSubscribed.get());
    }
}

