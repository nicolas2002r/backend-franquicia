package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.UpdateProductStockCommand;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductStockCommandTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UpdateProductStockCommand command;

    @Test
    void executeWithValidDataShouldUpdateProductStock() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productId = "product789";
        int newStock = 100;

        BranchDTO branch = new BranchDTO(branchId, franchiseId, "Branch Name");
        ProductDTO existingProduct = new ProductDTO(productId, branchId, "Product", 50);
        ProductDTO updatedProduct = new ProductDTO(productId, branchId, "Product", newStock);

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branch));
        when(productRepository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.just(existingProduct));
        when(productRepository.save(any(ProductDTO.class))).thenReturn(Mono.just(updatedProduct));

        StepVerifier.create(command.execute(franchiseId, branchId, productId, newStock))
                .expectNext(updatedProduct)
                .verifyComplete();
    }

    @Test
    void executeWithNegativeStockShouldThrowValidationException() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productId = "product789";
        int newStock = -5;

        // Stubs para evitar NPE en caso de que la validación no falle (aunque debería)
        when(branchRepository.findByIdAndFranchiseId(anyString(), anyString()))
                .thenReturn(Mono.just(new BranchDTO(branchId, franchiseId, "Branch")));
        when(productRepository.findByIdAndBranchId(anyString(), anyString()))
                .thenReturn(Mono.just(new ProductDTO(productId, branchId, "Product", 10)));

        StepVerifier.create(command.execute(franchiseId, branchId, productId, newStock))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals(Messages.STOCK_NEGATIVE))
                .verify();
    }

    @Test
    void executeWhenBranchNotFoundShouldThrowNotFoundException() {
        String franchiseId = "franchise123";
        String branchId = "nonexistent";
        String productId = "product789";
        int newStock = 100;

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.empty());
        // Stub para product por si acaso (aunque no debería llegar)
        when(productRepository.findByIdAndBranchId(anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(command.execute(franchiseId, branchId, productId, newStock))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("No se encontró sucursal para franquicia"))
                .verify();
    }

    @Test
    void executeWhenProductNotFoundShouldThrowNotFoundException() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productId = "nonexistent";
        int newStock = 100;

        BranchDTO branch = new BranchDTO(branchId, franchiseId, "Branch Name");

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branch));
        when(productRepository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.empty());

        StepVerifier.create(command.execute(franchiseId, branchId, productId, newStock))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Producto no encontrado en la sucursal"))
                .verify();
    }
}