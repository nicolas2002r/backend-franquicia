package co.com.bancolombia.usecase;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.MaxStockByBranchDTO;
import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FranchiseUseCase useCase;

    @Test
    void createFranchiseShouldReturnSavedFranchise() {
        String name = "New Franchise";
        FranchiseDTO saved = new FranchiseDTO("id123", name);

        when(franchiseRepository.existsByName(name)).thenReturn(Mono.just(false));
        when(franchiseRepository.save(any(FranchiseDTO.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.createFranchise(name))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void updateFranchiseNameShouldReturnUpdatedFranchise() {
        String franchiseId = "franchise123";
        String newName = "Updated Name";
        FranchiseDTO updated = new FranchiseDTO(franchiseId, newName);

        when(franchiseRepository.existsByName(newName)).thenReturn(Mono.just(false));
        when(franchiseRepository.updateName(franchiseId, newName)).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.updateFranchiseName(franchiseId, newName))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    void addBranchShouldReturnSavedBranch() {
        String franchiseId = "franchise123";
        String branchName = "New Branch";
        FranchiseDTO franchise = new FranchiseDTO(franchiseId, "Franchise");
        BranchDTO saved = new BranchDTO("branch456", franchiseId, branchName);

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.save(any(BranchDTO.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.addBranch(franchiseId, branchName))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void updateBranchNameShouldReturnUpdatedBranch() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String newName = "Updated Branch";
        BranchDTO updated = new BranchDTO(branchId, franchiseId, newName);

        when(branchRepository.updateName(branchId, franchiseId, newName)).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.updateBranchName(franchiseId, branchId, newName))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    void addProductShouldReturnSavedProduct() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productName = "New Product";
        int stock = 10;
        BranchDTO branch = new BranchDTO(branchId, franchiseId, "Branch");
        ProductDTO saved = new ProductDTO("product789", branchId, productName, stock);

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branch));
        when(productRepository.save(any(ProductDTO.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.addProduct(franchiseId, branchId, productName, stock))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void deleteProductShouldComplete() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productId = "product789";
        BranchDTO branch = new BranchDTO(branchId, franchiseId, "Branch");
        ProductDTO product = new ProductDTO(productId, branchId, "Product", 5);

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branch));
        when(productRepository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.just(product));
        when(productRepository.deleteByIdAndBranchId(productId, branchId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteProduct(franchiseId, branchId, productId))
                .verifyComplete();
    }

    @Test
    void updateProductStockShouldReturnUpdatedProduct() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productId = "product789";
        int newStock = 20;
        BranchDTO branch = new BranchDTO(branchId, franchiseId, "Branch");
        ProductDTO existing = new ProductDTO(productId, branchId, "Product", 10);
        ProductDTO updated = new ProductDTO(productId, branchId, "Product", newStock);

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branch));
        when(productRepository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.just(existing));
        when(productRepository.save(any(ProductDTO.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.updateProductStock(franchiseId, branchId, productId, newStock))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    void updateProductNameShouldReturnUpdatedProduct() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String productId = "product789";
        String newName = "Updated Product";
        BranchDTO branch = new BranchDTO(branchId, franchiseId, "Branch");
        ProductDTO existing = new ProductDTO(productId, branchId, "Old", 15);
        ProductDTO updated = new ProductDTO(productId, branchId, newName, 15);

        when(branchRepository.findByIdAndFranchiseId(branchId, franchiseId)).thenReturn(Mono.just(branch));
        when(productRepository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.just(existing));
        when(productRepository.save(any(ProductDTO.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.updateProductName(franchiseId, branchId, productId, newName))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    void getMaxStockByBranchShouldReturnFlux() {
        String franchiseId = "franchise123";
        FranchiseDTO franchise = new FranchiseDTO(franchiseId, "Franchise");
        BranchDTO branch1 = new BranchDTO("branch1", franchiseId, "Branch One");
        BranchDTO branch2 = new BranchDTO("branch2", franchiseId, "Branch Two");
        ProductDTO product1 = new ProductDTO("prod1", "branch1", "Product A", 100);
        ProductDTO product2 = new ProductDTO("prod2", "branch2", "Product B", 200);
        MaxStockByBranchDTO dto1 = new MaxStockByBranchDTO();
        dto1.setBranchId("branch1");
        dto1.setBranchName("Branch One");
        dto1.setProductId("prod1");
        dto1.setProductName("Product A");
        dto1.setStock(100);
        MaxStockByBranchDTO dto2 = new MaxStockByBranchDTO();
        dto2.setBranchId("branch2");
        dto2.setBranchName("Branch Two");
        dto2.setProductId("prod2");
        dto2.setProductName("Product B");
        dto2.setStock(200);

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId(franchiseId)).thenReturn(Flux.just(branch1, branch2));
        when(productRepository.findTopByBranchIdOrderByStockDesc("branch1")).thenReturn(Mono.just(product1));
        when(productRepository.findTopByBranchIdOrderByStockDesc("branch2")).thenReturn(Mono.just(product2));

        StepVerifier.create(useCase.getMaxStockByBranch(franchiseId))
                .expectNextMatches(d -> d.getBranchId().equals("branch1") && d.getStock() == 100)
                .expectNextMatches(d -> d.getBranchId().equals("branch2") && d.getStock() == 200)
                .verifyComplete();
    }
}
