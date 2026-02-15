package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.AddProductCommand;
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

class AddProductCommandTest {

    @Test
    void executeShouldValidateBranchTrimNameSaveProductAndAuditOnNextAndOnComplete() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        AddProductCommand command = new AddProductCommand(branchRepository, productRepository, audit);

        String franchiseId = "f1";
        String branchId = "b1";
        String rawName = "   Producto X   ";
        String trimmed = "Producto X";
        int stock = 10;

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId))
                .thenReturn(Mono.just(new Branch(branchId, franchiseId, "Sucursal A")));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(inv -> {
                    Product p = inv.getArgument(0);
                    return Mono.just(new Product("p1", p.branchId(), p.name(), p.stock()));
                });

        StepVerifier.create(command.execute(franchiseId, branchId, rawName, stock))
                .assertNext(saved -> {
                    assertEquals("p1", saved.id());
                    assertEquals(branchId, saved.branchId());
                    assertEquals(trimmed, saved.name());
                    assertEquals(stock, saved.stock());
                })
                .verifyComplete();

        InOrder repoOrder = inOrder(branchRepository, productRepository);
        repoOrder.verify(branchRepository).findByIdAndFranchiseId(branchId, franchiseId);
        repoOrder.verify(productRepository).save(any(Product.class));
        repoOrder.verifyNoMoreInteractions();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product toSave = captor.getValue();
        assertNull(toSave.id());
        assertEquals(branchId, toSave.branchId());
        assertEquals(trimmed, toSave.name());
        assertEquals(stock, toSave.stock());

        InOrder auditOrder = inOrder(auditLogger);
        auditOrder.verify(auditLogger).info("onNext addProduct");
        auditOrder.verify(auditLogger).info("onComplete addProduct");
        auditOrder.verifyNoMoreInteractions();
    }

    @Test
    void executeShouldFailValidationWhenStockNegativeAndNotCallReposAndAuditOnError() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        AddProductCommand command = new AddProductCommand(branchRepository, productRepository, audit);

        String franchiseId = "f1";
        String branchId = "b1";
        int stock = -1;

        StepVerifier.create(command.execute(franchiseId, branchId, "Prod", stock))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    assertEquals(Messages.STOCK_NEGATIVE, err.getMessage());
                })
                .verify();

        verifyNoInteractions(branchRepository);
        verifyNoInteractions(productRepository);

        verify(auditLogger).error(eq("onError addProduct"), any(ValidationException.class));
        verify(auditLogger, never()).info(anyString());
        verifyNoMoreInteractions(auditLogger);
    }

    @Test
    void executeShouldFailValidationWhenNameBlankAndNotCallReposAndAuditOnError() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        AddProductCommand command = new AddProductCommand(branchRepository, productRepository, audit);

        StepVerifier.create(command.execute("f1", "b1", "   ", 0))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof ValidationException);
                    assertEquals(Messages.PRODUCT_NAME_REQUIRED, err.getMessage());
                })
                .verify();

        verifyNoInteractions(branchRepository);
        verifyNoInteractions(productRepository);

        verify(auditLogger).error(eq("onError addProduct"), any(ValidationException.class));
        verify(auditLogger, never()).info(anyString());
        verifyNoMoreInteractions(auditLogger);
    }

    @Test
    void executeShouldFailWhenBranchNotFoundAndNotSaveProductAndAuditOnError() {
        BranchRepository branchRepository = mock(BranchRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        AuditLogger auditLogger = mock(AuditLogger.class);

        when(auditLogger.info(anyString())).thenReturn(Mono.empty());
        when(auditLogger.error(anyString(), any(Throwable.class))).thenReturn(Mono.empty());

        AuditSupport audit = new AuditSupport(auditLogger);
        AddProductCommand command = new AddProductCommand(branchRepository, productRepository, audit);

        String franchiseId = "f1";
        String branchId = "missing";
        String name = "Producto";
        int stock = 5;

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId))
                .thenReturn(Mono.empty());

        StepVerifier.create(command.execute(franchiseId, branchId, name, stock))
                .expectErrorSatisfies(err -> {
                    assertTrue(err instanceof NotFoundException);
                    assertEquals(Messages.NO_SUCURSAL, err.getMessage());
                })
                .verify();

        verify(branchRepository).findByIdAndFranchiseId(branchId, franchiseId);
        verifyNoMoreInteractions(branchRepository);

        verify(productRepository, never()).save(any());

        verify(auditLogger).error(eq("onError addProduct"), any(NotFoundException.class));
        verify(auditLogger, never()).info(anyString());
        verifyNoMoreInteractions(auditLogger);
    }
}

