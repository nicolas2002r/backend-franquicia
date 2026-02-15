package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.MaxStockByBranch;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.GetMaxStockByBranchQuery;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GetMaxStockByBranchQueryTest {

    @Test
    void execute_shouldReturnMaxStockPerBranch_andLogOnNextForEach_andLogOnCompleteOnce() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onNextSubscribed = new AtomicInteger(0);
        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);

        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext getMaxStockByBranch")) {
                return Mono.fromRunnable(onNextSubscribed::incrementAndGet);
            }
            if (msg.equals("onComplete getMaxStockByBranch")) {
                return Mono.fromRunnable(onCompleteSubscribed::incrementAndGet);
            }
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(onErrorSubscribed::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        AuditSupport audit = new AuditSupport(auditLogger);
        GetMaxStockByBranchQuery query = new GetMaxStockByBranchQuery(
                franchiseRepository, branchRepository, productRepository, audit
        );

        String franchiseId = "f1";

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(new Franchise(franchiseId, "Franquicia A")));

        Branch b1 = new Branch("b1", franchiseId, "Sucursal 1");
        Branch b2 = new Branch("b2", franchiseId, "Sucursal 2");

        when(branchRepository.findByFranchiseId(franchiseId))
                .thenReturn(Flux.just(b1, b2));

        when(productRepository.findTopByBranchIdOrderByStockDesc("b1"))
                .thenReturn(Mono.just(new Product("p1", "b1", "Prod 1", 10)));

        when(productRepository.findTopByBranchIdOrderByStockDesc("b2"))
                .thenReturn(Mono.just(new Product("p2", "b2", "Prod 2", 50)));

        StepVerifier.create(query.execute(franchiseId).collectList())
                .assertNext(list -> {
                    assertEquals(2, list.size());

                    MaxStockByBranch r1 = list.stream().filter(x -> x.branchId().equals("b1")).findFirst().orElseThrow();
                    assertEquals("Sucursal 1", r1.branchName());
                    assertEquals("p1", r1.productId());
                    assertEquals("Prod 1", r1.productName());
                    assertEquals(10, r1.stock());

                    MaxStockByBranch r2 = list.stream().filter(x -> x.branchId().equals("b2")).findFirst().orElseThrow();
                    assertEquals("Sucursal 2", r2.branchName());
                    assertEquals("p2", r2.productId());
                    assertEquals("Prod 2", r2.productName());
                    assertEquals(50, r2.stock());
                })
                .verifyComplete();

        assertEquals(2, onNextSubscribed.get(), "Debe loguear onNext por cada branch resultante");
        assertEquals(1, onCompleteSubscribed.get(), "Debe loguear onComplete una sola vez al final");
        assertEquals(0, onErrorSubscribed.get(), "No debe loguear onError en happy path");
    }

    @Test
    void execute_shouldFail_whenFranchiseNotFound_andLogOnError() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onNextSubscribed = new AtomicInteger(0);
        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);
        AtomicInteger branchesSubscribed = new AtomicInteger(0);

        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext getMaxStockByBranch")) return Mono.fromRunnable(onNextSubscribed::incrementAndGet);
            if (msg.equals("onComplete getMaxStockByBranch")) return Mono.fromRunnable(onCompleteSubscribed::incrementAndGet);
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(onErrorSubscribed::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        when(branchRepository.findByFranchiseId(anyString()))
                .thenReturn(Flux.defer(() -> {
                    branchesSubscribed.incrementAndGet();
                    return Flux.empty();
                }));

        AuditSupport audit = new AuditSupport(auditLogger);
        GetMaxStockByBranchQuery query = new GetMaxStockByBranchQuery(
                franchiseRepository, branchRepository, productRepository, audit
        );

        String franchiseId = "f404";

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(query.execute(franchiseId))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.FRANQUICIA_NO_ENCONTRADA + franchiseId, err.getMessage());
                })
                .verify();

        assertEquals(0, onNextSubscribed.get());
        assertEquals(0, onCompleteSubscribed.get(), "En error no debe suscribirse onComplete");
        assertEquals(1, onErrorSubscribed.get());
        assertEquals(0, branchesSubscribed.get(), "No debe consultar branches si no existe la franquicia");
    }

    @Test
    void execute_shouldFail_whenNoBranchesForFranchise_andLogOnError() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onNextSubscribed = new AtomicInteger(0);
        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);
        AtomicInteger branchesSubscribed = new AtomicInteger(0);

        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext getMaxStockByBranch")) return Mono.fromRunnable(onNextSubscribed::incrementAndGet);
            if (msg.equals("onComplete getMaxStockByBranch")) return Mono.fromRunnable(onCompleteSubscribed::incrementAndGet);
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(onErrorSubscribed::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        AuditSupport audit = new AuditSupport(auditLogger);
        GetMaxStockByBranchQuery query = new GetMaxStockByBranchQuery(
                franchiseRepository, branchRepository, productRepository, audit
        );

        String franchiseId = "f1";

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(new Franchise(franchiseId, "Franquicia A")));

        when(branchRepository.findByFranchiseId(franchiseId))
                .thenReturn(Flux.defer(() -> {
                    branchesSubscribed.incrementAndGet();
                    return Flux.empty();
                }));

        StepVerifier.create(query.execute(franchiseId))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals("No branches for franchise: " + franchiseId, err.getMessage());
                })
                .verify();

        assertEquals(1, branchesSubscribed.get());
        assertEquals(0, onNextSubscribed.get());
        assertEquals(0, onCompleteSubscribed.get());
        assertEquals(1, onErrorSubscribed.get());
    }

    @Test
    void execute_shouldFail_whenBranchHasNoProducts_andLogOnError() {
        FranchiseRepository franchiseRepository = mock(FranchiseRepository.class);
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        AtomicInteger onNextSubscribed = new AtomicInteger(0);
        AtomicInteger onCompleteSubscribed = new AtomicInteger(0);
        AtomicInteger onErrorSubscribed = new AtomicInteger(0);
        AtomicInteger topProductSubscribed = new AtomicInteger(0);

        doAnswer(inv -> {
            String msg = inv.getArgument(0, String.class);
            if (msg.equals("onNext getMaxStockByBranch")) return Mono.fromRunnable(onNextSubscribed::incrementAndGet);
            if (msg.equals("onComplete getMaxStockByBranch")) return Mono.fromRunnable(onCompleteSubscribed::incrementAndGet);
            return Mono.empty();
        }).when(auditLogger).info(anyString());

        doAnswer(inv -> Mono.fromRunnable(onErrorSubscribed::incrementAndGet))
                .when(auditLogger).error(anyString(), any(Throwable.class));

        AuditSupport audit = new AuditSupport(auditLogger);
        GetMaxStockByBranchQuery query = new GetMaxStockByBranchQuery(
                franchiseRepository, branchRepository, productRepository, audit
        );

        String franchiseId = "f1";
        Branch b1 = new Branch("b1", franchiseId, "Sucursal 1");

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(new Franchise(franchiseId, "Franquicia A")));

        when(branchRepository.findByFranchiseId(franchiseId))
                .thenReturn(Flux.just(b1));

        when(productRepository.findTopByBranchIdOrderByStockDesc("b1"))
                .thenReturn(Mono.defer(() -> {
                    topProductSubscribed.incrementAndGet();
                    return Mono.empty();
                }));

        StepVerifier.create(query.execute(franchiseId))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals("Branch has no products: b1", err.getMessage());
                })
                .verify();

        assertEquals(1, topProductSubscribed.get());
        assertEquals(0, onNextSubscribed.get(), "No emite items, no debe loguear onNext");
        assertEquals(0, onCompleteSubscribed.get(), "En error no debe suscribirse onComplete");
        assertEquals(1, onErrorSubscribed.get());
    }
}

