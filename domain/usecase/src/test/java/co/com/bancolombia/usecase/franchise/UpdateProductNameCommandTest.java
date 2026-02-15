package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.UpdateProductNameCommand;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UpdateProductNameCommandTest {

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
    void execute_shouldUpdateName_keepStock_andLogOnNextAndOnComplete() {
        BranchRepository branchRepo = mock(BranchRepository.class);
        ProductRepository productRepo = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateProductName");
        UpdateProductNameCommand cmd = new UpdateProductNameCommand(branchRepo, productRepo, audit);

        String franchiseId = "f1";
        String branchId = "b1";
        String productId = "p1";

        when(branchRepo.findByIdAndFranchiseId(branchId, franchiseId))
                .thenReturn(Mono.just(new Branch(branchId, franchiseId, "Sucursal 1")));

        when(productRepo.findByIdAndBranchId(productId, branchId))
                .thenReturn(Mono.just(new Product(productId, branchId, "Viejo", 10)));

        when(productRepo.save(any(Product.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0, Product.class)));

        StepVerifier.create(cmd.execute(franchiseId, branchId, productId, "  Nuevo  "))
                .assertNext(p -> {
                    assertEquals(productId, p.id());
                    assertEquals(branchId, p.branchId());
                    assertEquals("Nuevo", p.name());
                    assertEquals(10, p.stock());
                })
                .verifyComplete();

        verify(branchRepo).findByIdAndFranchiseId(branchId, franchiseId);
        verify(productRepo).findByIdAndBranchId(productId, branchId);

        verify(productRepo).save(argThat(p ->
                p.id().equals(productId)
                        && p.branchId().equals(branchId)
                        && p.name().equals("Nuevo")
                        && p.stock() == 10
        ));
        verifyNoMoreInteractions(branchRepo, productRepo);

        assertEquals(1, c.onNext.get());
        assertEquals(1, c.onComplete.get());
        assertEquals(0, c.onError.get());
    }

    @Test
    void execute_shouldFail_whenNameBlank_andNotTouchRepos_andLogOnError() {
        BranchRepository branchRepo = mock(BranchRepository.class);
        ProductRepository productRepo = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateProductName");
        UpdateProductNameCommand cmd = new UpdateProductNameCommand(branchRepo, productRepo, audit);

        StepVerifier.create(cmd.execute("f1", "b1", "p1", "   "))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    assertEquals(Messages.PRODUCT_NAME_REQUIRED, err.getMessage());
                })
                .verify();

        verifyNoInteractions(branchRepo, productRepo);

        assertEquals(0, c.onNext.get());
        assertEquals(0, c.onComplete.get());
        assertEquals(1, c.onError.get());
    }

    @Test
    void execute_shouldFail_whenBranchNotFound_andNotTouchProductRepo_andLogOnError() {
        BranchRepository branchRepo = mock(BranchRepository.class);
        ProductRepository productRepo = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateProductName");
        UpdateProductNameCommand cmd = new UpdateProductNameCommand(branchRepo, productRepo, audit);

        when(branchRepo.findByIdAndFranchiseId("b404", "f1")).thenReturn(Mono.empty());

        StepVerifier.create(cmd.execute("f1", "b404", "p1", "Nuevo"))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.NO_SUCURSAL, err.getMessage());
                })
                .verify();

        verify(branchRepo).findByIdAndFranchiseId("b404", "f1");
        verifyNoMoreInteractions(branchRepo);
        verifyNoInteractions(productRepo);

        assertEquals(0, c.onNext.get());
        assertEquals(0, c.onComplete.get());
        assertEquals(1, c.onError.get());
    }

    @Test
    void execute_shouldFail_whenProductNotFound_andNotCallSave_andLogOnError() {
        BranchRepository branchRepo = mock(BranchRepository.class);
        ProductRepository productRepo = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateProductName");
        UpdateProductNameCommand cmd = new UpdateProductNameCommand(branchRepo, productRepo, audit);

        when(branchRepo.findByIdAndFranchiseId("b1", "f1"))
                .thenReturn(Mono.just(new Branch("b1", "f1", "Sucursal 1")));

        when(productRepo.findByIdAndBranchId("p404", "b1"))
                .thenReturn(Mono.empty());

        StepVerifier.create(cmd.execute("f1", "b1", "p404", "Nuevo"))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.PRODUCTO_NO_ENCONTRADO, err.getMessage());
                })
                .verify();

        verify(branchRepo).findByIdAndFranchiseId("b1", "f1");
        verify(productRepo).findByIdAndBranchId("p404", "b1");
        verify(productRepo, never()).save(any());
        verifyNoMoreInteractions(branchRepo, productRepo);

        assertEquals(0, c.onNext.get());
        assertEquals(0, c.onComplete.get());
        assertEquals(1, c.onError.get());
    }

    @Test
    void execute_shouldPropagateSaveError_andLogOnError() {
        BranchRepository branchRepo = mock(BranchRepository.class);
        ProductRepository productRepo = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        AuditCounters c = new AuditCounters();

        AuditSupport audit = auditSupportWithCounters(auditLogger, c, "updateProductName");
        UpdateProductNameCommand cmd = new UpdateProductNameCommand(branchRepo, productRepo, audit);

        when(branchRepo.findByIdAndFranchiseId("b1", "f1"))
                .thenReturn(Mono.just(new Branch("b1", "f1", "Sucursal 1")));

        when(productRepo.findByIdAndBranchId("p1", "b1"))
                .thenReturn(Mono.just(new Product("p1", "b1", "Viejo", 10)));

        RuntimeException boom = new RuntimeException("save failed");
        when(productRepo.save(any(Product.class))).thenReturn(Mono.error(boom));

        StepVerifier.create(cmd.execute("f1", "b1", "p1", "Nuevo"))
                .expectErrorSatisfies(err -> assertSame(boom, err))
                .verify();

        verify(branchRepo).findByIdAndFranchiseId("b1", "f1");
        verify(productRepo).findByIdAndBranchId("p1", "b1");
        verify(productRepo).save(any(Product.class));
        verifyNoMoreInteractions(branchRepo, productRepo);

        assertEquals(0, c.onNext.get());
        assertEquals(0, c.onComplete.get());
        assertEquals(1, c.onError.get());
    }
}

