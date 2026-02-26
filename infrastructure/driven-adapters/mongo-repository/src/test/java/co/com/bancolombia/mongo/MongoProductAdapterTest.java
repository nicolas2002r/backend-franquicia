package co.com.bancolombia.mongo;

import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.mongo.document.ProductDocument;
import co.com.bancolombia.mongo.repository.ReactiveProductMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoProductAdapterTest {

    @Mock
    private ReactiveProductMongoRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MongoProductAdapter adapter;

    @Test
    void saveShouldMapAndSave() {
        ProductDTO dto = new ProductDTO("p1", "b1", "Product", 10);
        ProductDocument doc = new ProductDocument("p1", "b1", "Product", 10);
        ProductDocument savedDoc = new ProductDocument("p1", "b1", "Product", 10);

        when(objectMapper.map(dto, ProductDocument.class)).thenReturn(doc);
        when(repository.save(doc)).thenReturn(Mono.just(savedDoc));
        when(objectMapper.map(savedDoc, ProductDTO.class)).thenReturn(dto);

        StepVerifier.create(adapter.save(dto))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void findByIdAndBranchIdWhenFoundShouldReturnDTO() {
        String productId = "p1";
        String branchId = "b1";
        ProductDocument doc = new ProductDocument(productId, branchId, "Product", 10);
        ProductDTO dto = new ProductDTO(productId, branchId, "Product", 10);

        when(repository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.just(doc));
        when(objectMapper.map(doc, ProductDTO.class)).thenReturn(dto);

        StepVerifier.create(adapter.findByIdAndBranchId(productId, branchId))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void findByIdAndBranchIdWhenNotFoundShouldReturnEmpty() {
        String productId = "p1";
        String branchId = "b1";

        when(repository.findByIdAndBranchId(productId, branchId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByIdAndBranchId(productId, branchId))
                .verifyComplete();
    }

    @Test
    void deleteByIdAndBranchIdShouldCallRepository() {
        String productId = "p1";
        String branchId = "b1";

        when(repository.deleteByIdAndBranchId(productId, branchId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteByIdAndBranchId(productId, branchId))
                .verifyComplete();
    }

    @Test
    void findTopByBranchIdOrderByStockDescWhenFoundShouldReturnDTO() {
        String branchId = "b1";
        ProductDocument doc = new ProductDocument("p1", branchId, "Top", 100);
        ProductDTO dto = new ProductDTO("p1", branchId, "Top", 100);

        when(repository.findTopByBranchIdOrderByStockDesc(branchId)).thenReturn(Mono.just(doc));
        when(objectMapper.map(doc, ProductDTO.class)).thenReturn(dto);

        StepVerifier.create(adapter.findTopByBranchIdOrderByStockDesc(branchId))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void findTopByBranchIdOrderByStockDescWhenNotFoundShouldReturnEmpty() {
        String branchId = "b1";

        when(repository.findTopByBranchIdOrderByStockDesc(branchId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findTopByBranchIdOrderByStockDesc(branchId))
                .verifyComplete();
    }

    @Test
    void findByBranchIdShouldReturnFluxOfDTOs() {
        String branchId = "b1";
        ProductDocument doc1 = new ProductDocument("p1", branchId, "A", 5);
        ProductDocument doc2 = new ProductDocument("p2", branchId, "B", 10);
        ProductDTO dto1 = new ProductDTO("p1", branchId, "A", 5);
        ProductDTO dto2 = new ProductDTO("p2", branchId, "B", 10);

        when(repository.findByBranchId(branchId)).thenReturn(Flux.just(doc1, doc2));
        when(objectMapper.map(doc1, ProductDTO.class)).thenReturn(dto1);
        when(objectMapper.map(doc2, ProductDTO.class)).thenReturn(dto2);

        StepVerifier.create(adapter.findByBranchId(branchId))
                .expectNext(dto1, dto2)
                .verifyComplete();
    }

    @Test
    void findByBranchIdWhenNoneShouldReturnEmptyFlux() {
        String branchId = "b1";

        when(repository.findByBranchId(branchId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByBranchId(branchId))
                .verifyComplete();
    }
}

