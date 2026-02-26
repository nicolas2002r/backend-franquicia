package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.MaxStockByBranchDTO;
import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.command.GetMaxStockByBranchQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMaxStockByBranchQueryTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetMaxStockByBranchQuery query;

    @Test
    void executeWhenFranchiseExistsAndBranchesHaveProductsShouldReturnMaxStock() {
        String franchiseId = "franchise123";
        FranchiseDTO franchise = new FranchiseDTO(franchiseId, "Test Franchise");

        BranchDTO branch1 = new BranchDTO("branch1", franchiseId, "Branch One");
        BranchDTO branch2 = new BranchDTO("branch2", franchiseId, "Branch Two");

        ProductDTO product1 = new ProductDTO("prod1", "branch1", "Product A", 100);
        ProductDTO product2 = new ProductDTO("prod2", "branch2", "Product B", 200);

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId(franchiseId)).thenReturn(Flux.just(branch1, branch2));
        when(productRepository.findTopByBranchIdOrderByStockDesc("branch1")).thenReturn(Mono.just(product1));
        when(productRepository.findTopByBranchIdOrderByStockDesc("branch2")).thenReturn(Mono.just(product2));

        Flux<MaxStockByBranchDTO> result = query.execute(franchiseId);

        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getBranchId().equals("branch1") &&
                                dto.getBranchName().equals("Branch One") &&
                                dto.getProductId().equals("prod1") &&
                                dto.getProductName().equals("Product A") &&
                                dto.getStock() == 100)
                .expectNextMatches(dto ->
                        dto.getBranchId().equals("branch2") &&
                                dto.getBranchName().equals("Branch Two") &&
                                dto.getProductId().equals("prod2") &&
                                dto.getProductName().equals("Product B") &&
                                dto.getStock() == 200)
                .verifyComplete();
    }

    @Test
    void executeWhenFranchiseNotFoundShouldThrowError() {
        String franchiseId = "nonexistent";

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        Flux<MaxStockByBranchDTO> result = query.execute(franchiseId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Franquicia no encontrada: nonexistent"))
                .verify();
    }

    @Test
    void executeWhenFranchiseHasNoBranchesShouldThrowError() {
        String franchiseId = "franchise123";
        FranchiseDTO franchise = new FranchiseDTO(franchiseId, "Test Franchise");

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId(franchiseId)).thenReturn(Flux.empty());

        Flux<MaxStockByBranchDTO> result = query.execute(franchiseId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("No branches for franchise: " + franchiseId))
                .verify();
    }

    @Test
    void executeWhenBranchHasNoProductsShouldThrowError() {
        String franchiseId = "franchise123";
        FranchiseDTO franchise = new FranchiseDTO(franchiseId, "Test Franchise");
        BranchDTO branch = new BranchDTO("branch1", franchiseId, "Branch One");

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId(franchiseId)).thenReturn(Flux.just(branch));
        when(productRepository.findTopByBranchIdOrderByStockDesc("branch1")).thenReturn(Mono.empty());

        Flux<MaxStockByBranchDTO> result = query.execute(franchiseId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Branch has no products: branch1"))
                .verify();
    }
}

