package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.AddProductCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProductCommandTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AddProductCommand addProductCommand;

    @Test
    void executeWithValidDataShouldSaveProduct() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productName = "New Product";
        int stock = 10;
        ProductDTO savedProduct = new ProductDTO("product789", branchId, productName, stock);

        BranchDTO branchMock = new BranchDTO(branchId, franchiseId, "Branch Name");
        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branchMock));
        when(productRepository.save(any(ProductDTO.class))).thenReturn(Mono.just(savedProduct));

        Mono<ProductDTO> result = addProductCommand.execute(franchiseId, branchId, productName, stock);

        StepVerifier.create(result)
                .expectNext(savedProduct)
                .verifyComplete();
    }

    @Test
    void executeWithNegativeStockShouldReturnError() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productName = "New Product";
        int stock = -5;

        Mono<ProductDTO> result = addProductCommand.execute(franchiseId, branchId, productName, stock);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Stock cannot be negative"))
                .verify();
    }

    @Test
    void executeWithNullProductNameShouldReturnError() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        int stock = 10;

        Mono<ProductDTO> result = addProductCommand.execute(franchiseId, branchId, null, stock);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Product name is required"))
                .verify();
    }

    @Test
    void executeWithEmptyProductNameShouldReturnError() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        int stock = 10;

        Mono<ProductDTO> result = addProductCommand.execute(franchiseId, branchId, "   ", stock);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Product name is required"))
                .verify();
    }

    @Test
    void executeWithNonExistentBranchShouldReturnError() {
        String franchiseId = "franchise123";
        String branchId = "nonexistent";
        String productName = "New Product";
        int stock = 10;

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.empty());

        Mono<ProductDTO> result = addProductCommand.execute(franchiseId, branchId, productName, stock);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("No se encontró sucursal para franquicia"))
                .verify();
    }
}

