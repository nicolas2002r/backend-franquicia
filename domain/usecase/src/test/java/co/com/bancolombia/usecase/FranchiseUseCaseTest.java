package co.com.bancolombia.usecase;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.MaxStockByBranch;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class FranchiseUseCaseTest {

    private static <T> T mockReactive(Class<T> type) {
        return Mockito.mock(type, invocation -> {
            Class<?> rt = invocation.getMethod().getReturnType();

            if (rt.equals(Mono.class)) return Mono.empty();
            if (rt.equals(Flux.class)) return Flux.empty();
            if (Publisher.class.isAssignableFrom(rt)) return Flux.empty();

            if (rt.equals(boolean.class)) return false;
            if (rt.equals(int.class)) return 0;
            if (rt.equals(long.class)) return 0L;
            if (rt.equals(double.class)) return 0D;
            if (rt.equals(float.class)) return 0F;
            if (rt.equals(short.class)) return (short) 0;
            if (rt.equals(byte.class)) return (byte) 0;
            if (rt.equals(char.class)) return (char) 0;

            return null;
        });
    }

    @Test
    void shouldConstructAndExposeAllOperationsAsReactiveTypes() {
        FranchiseRepository franchiseRepository = mockReactive(FranchiseRepository.class);
        BranchRepository branchRepository = mockReactive(BranchRepository.class);
        ProductRepository productRepository = mockReactive(ProductRepository.class);
        AuditLogger auditLogger = mockReactive(AuditLogger.class);

        FranchiseUseCase useCase = assertDoesNotThrow(() ->
                new FranchiseUseCase(franchiseRepository, branchRepository, productRepository, auditLogger)
        );
        assertNotNull(useCase);

        Mono<Franchise> createFranchise = assertDoesNotThrow(() -> useCase.createFranchise("Franquicia A"));
        assertNotNull(createFranchise);

        Mono<Franchise> renameFranchise = assertDoesNotThrow(() ->
                useCase.updateFranchiseName("franchiseId", "Nuevo Nombre")
        );
        assertNotNull(renameFranchise);

        Mono<Branch> addBranch = assertDoesNotThrow(() ->
                useCase.addBranch("franchiseId", "Sucursal 1")
        );
        assertNotNull(addBranch);

        Mono<Branch> renameBranch = assertDoesNotThrow(() ->
                useCase.updateBranchName("franchiseId", "branchId", "Sucursal Renombrada")
        );
        assertNotNull(renameBranch);

        Mono<Product> addProduct = assertDoesNotThrow(() ->
                useCase.addProduct("franchiseId", "branchId", "Producto 1", 10)
        );
        assertNotNull(addProduct);

        Mono<Void> deleteProduct = assertDoesNotThrow(() ->
                useCase.deleteProduct("franchiseId", "branchId", "productId")
        );
        assertNotNull(deleteProduct);

        Mono<Product> updateStock = assertDoesNotThrow(() ->
                useCase.updateProductStock("franchiseId", "branchId", "productId", 99)
        );
        assertNotNull(updateStock);

        Mono<Product> renameProduct = assertDoesNotThrow(() ->
                useCase.updateProductName("franchiseId", "branchId", "productId", "Producto Renombrado")
        );
        assertNotNull(renameProduct);

        Flux<MaxStockByBranch> maxStock = assertDoesNotThrow(() ->
                useCase.getMaxStockByBranch("franchiseId")
        );
        assertNotNull(maxStock);
    }
}
