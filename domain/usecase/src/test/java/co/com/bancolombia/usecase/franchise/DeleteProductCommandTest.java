package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.DeleteProductCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProductCommandTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeleteProductCommand deleteProductCommand;

    @Test
    void executeWithValidDataShouldDeleteProduct() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productId = "product789";

        BranchDTO branchMock = new BranchDTO(branchId, franchiseId, "Branch Name");
        ProductDTO productMock = new ProductDTO(productId, branchId, "Product", 10);

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branchMock));
        when(productRepository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.just(productMock));
        when(productRepository.deleteByIdAndBranchId(productId, branchId)).thenReturn(Mono.empty());

        Mono<Void> result = deleteProductCommand.execute(franchiseId, branchId, productId);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void executeWithNonExistentBranchShouldReturnError() {
        String franchiseId = "franchise123";
        String branchId = "nonexistent";
        String productId = "product789";

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.empty());
        when(productRepository.findByIdAndBranchId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Void> result = deleteProductCommand.execute(franchiseId, branchId, productId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("No se encontró sucursal para franquicia"))
                .verify();
    }

    @Test
    void executeWithNonExistentProductShouldReturnError() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productId = "nonexistent";

        BranchDTO branchMock = new BranchDTO(branchId, franchiseId, "Branch Name");

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branchMock));
        when(productRepository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.empty());

        Mono<Void> result = deleteProductCommand.execute(franchiseId, branchId, productId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Producto no encontrado en la sucursal"))
                .verify();
    }
}
