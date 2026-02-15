package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.DeleteProductCommand;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeleteProductCommandTest {

    @Test
    void executeShouldCompleteWhenEverythingExistsAndShouldSubscribeOnCompleteLog() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);
        AtomicInteger findProductSubscribed = new AtomicInteger(0);
        AtomicInteger deleteSubscribed = new AtomicInteger(0);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.info(eq("onComplete deleteProduct")))
                .thenReturn(Mono.fromRunnable(onCompleteSubscribed::incrementAndGet));

        when(auditLogger.error(anyString(), any(Throwable.class)))
                .thenReturn(Mono.fromRunnable(onErrorSubscribed::incrementAndGet));

        AuditSupport audit = new AuditSupport(auditLogger);
        DeleteProductCommand command = new DeleteProductCommand(branchRepository, productRepository, audit);

        String franchiseId = "f1";
        String branchId = "b1";
        String productId = "p1";

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId))
                .thenReturn(Mono.just(new Branch(branchId, franchiseId, "Sucursal 1")));

        when(productRepository.findByIdAndBranchId(productId, branchId))
                .thenReturn(Mono.defer(() -> {
                    findProductSubscribed.incrementAndGet();
                    return Mono.just(new Product(productId, branchId, "Prod 1", 10));
                }));

        when(productRepository.deleteByIdAndBranchId(productId, branchId))
                .thenReturn(Mono.defer(() -> {
                    deleteSubscribed.incrementAndGet();
                    return Mono.empty();
                }));

        StepVerifier.create(command.execute(franchiseId, branchId, productId))
                .verifyComplete();

        assertEquals(1, findProductSubscribed.get());
        assertEquals(1, deleteSubscribed.get());
        assertEquals(1, onCompleteSubscribed.get());
        assertEquals(0, onErrorSubscribed.get());

        verify(productRepository).deleteByIdAndBranchId(productId, branchId);
    }

    @Test
    void executeShouldFailWhenBranchNotFoundAndShouldNotSubscribeProductLookupNorDelete() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);
        AtomicInteger findProductSubscribed = new AtomicInteger(0);
        AtomicInteger deleteSubscribed = new AtomicInteger(0);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.info(eq("onComplete deleteProduct")))
                .thenReturn(Mono.fromRunnable(onCompleteSubscribed::incrementAndGet));
        when(auditLogger.error(anyString(), any(Throwable.class)))
                .thenReturn(Mono.fromRunnable(onErrorSubscribed::incrementAndGet));

        AuditSupport audit = new AuditSupport(auditLogger);
        DeleteProductCommand command = new DeleteProductCommand(branchRepository, productRepository, audit);

        String franchiseId = "f1";
        String branchId = "b404";
        String productId = "p1";

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId))
                .thenReturn(Mono.empty());

        when(productRepository.findByIdAndBranchId(anyString(), anyString()))
                .thenReturn(Mono.defer(() -> {
                    findProductSubscribed.incrementAndGet();
                    return Mono.just(new Product("x", "y", "z", 1));
                }));

        when(productRepository.deleteByIdAndBranchId(anyString(), anyString()))
                .thenReturn(Mono.defer(() -> {
                    deleteSubscribed.incrementAndGet();
                    return Mono.empty();
                }));

        StepVerifier.create(command.execute(franchiseId, branchId, productId))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.NO_SUCURSAL, err.getMessage());
                })
                .verify();

        assertEquals(0, findProductSubscribed.get(), "No debería suscribirse búsqueda de producto si no hay sucursal");
        assertEquals(0, deleteSubscribed.get(), "No debería intentar delete si no hay sucursal");
        assertEquals(0, onCompleteSubscribed.get(), "No debería suscribirse onComplete si falla");
        assertEquals(1, onErrorSubscribed.get(), "Debe loguear onError");
    }

    @Test
    void executeShouldFailWhenProductNotFoundAndShouldNotSubscribeDelete() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);
        AtomicInteger findProductSubscribed = new AtomicInteger(0);
        AtomicInteger deleteSubscribed = new AtomicInteger(0);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.info(eq("onComplete deleteProduct")))
                .thenReturn(Mono.fromRunnable(onCompleteSubscribed::incrementAndGet));
        when(auditLogger.error(anyString(), any(Throwable.class)))
                .thenReturn(Mono.fromRunnable(onErrorSubscribed::incrementAndGet));

        AuditSupport audit = new AuditSupport(auditLogger);
        DeleteProductCommand command = new DeleteProductCommand(branchRepository, productRepository, audit);

        String franchiseId = "f1";
        String branchId = "b1";
        String productId = "p404";

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId))
                .thenReturn(Mono.just(new Branch(branchId, franchiseId, "Sucursal 1")));

        when(productRepository.findByIdAndBranchId(productId, branchId))
                .thenReturn(Mono.defer(() -> {
                    findProductSubscribed.incrementAndGet();
                    return Mono.empty();
                }));

        when(productRepository.deleteByIdAndBranchId(anyString(), anyString()))
                .thenReturn(Mono.defer(() -> {
                    deleteSubscribed.incrementAndGet();
                    return Mono.empty();
                }));

        StepVerifier.create(command.execute(franchiseId, branchId, productId))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.PRODUCTO_NO_ENCONTRADO, err.getMessage());
                })
                .verify();

        assertEquals(1, findProductSubscribed.get());
        assertEquals(0, deleteSubscribed.get(), "No debe borrar si no encontró producto");
        assertEquals(0, onCompleteSubscribed.get());
        assertEquals(1, onErrorSubscribed.get());
    }

    @Test
    void executeShouldPropagateDeleteErrorAndShouldNotSubscribeOnComplete() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);
        AtomicInteger findProductSubscribed = new AtomicInteger(0);
        AtomicInteger deleteSubscribed = new AtomicInteger(0);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.info(eq("onComplete deleteProduct")))
                .thenReturn(Mono.fromRunnable(onCompleteSubscribed::incrementAndGet));
        when(auditLogger.error(anyString(), any(Throwable.class)))
                .thenReturn(Mono.fromRunnable(onErrorSubscribed::incrementAndGet));

        AuditSupport audit = new AuditSupport(auditLogger);
        DeleteProductCommand command = new DeleteProductCommand(branchRepository, productRepository, audit);

        String franchiseId = "f1";
        String branchId = "b1";
        String productId = "p1";

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId))
                .thenReturn(Mono.just(new Branch(branchId, franchiseId, "Sucursal 1")));

        when(productRepository.findByIdAndBranchId(productId, branchId))
                .thenReturn(Mono.defer(() -> {
                    findProductSubscribed.incrementAndGet();
                    return Mono.just(new Product(productId, branchId, "Prod 1", 10));
                }));

        RuntimeException boom = new RuntimeException("delete failed");
        when(productRepository.deleteByIdAndBranchId(productId, branchId))
                .thenReturn(Mono.defer(() -> {
                    deleteSubscribed.incrementAndGet();
                    return Mono.error(boom);
                }));

        StepVerifier.create(command.execute(franchiseId, branchId, productId))
                .expectErrorSatisfies(err -> assertSame(boom, err))
                .verify();

        assertEquals(1, findProductSubscribed.get());
        assertEquals(1, deleteSubscribed.get());
        assertEquals(0, onCompleteSubscribed.get());
        assertEquals(1, onErrorSubscribed.get());
    }
}
