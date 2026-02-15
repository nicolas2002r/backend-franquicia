package co.com.bancolombia.mongo;

import co.com.bancolombia.model.Product;
import co.com.bancolombia.mongo.mapper.ProductMapper;
import co.com.bancolombia.mongo.repository.ReactiveProductMongoRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MongoProductAdapterTest {

    @Test
    void save_shouldMapToDoc_andBackToDomain() {
        ReactiveProductMongoRepository repo = mock(ReactiveProductMongoRepository.class);
        MongoProductAdapter adapter = new MongoProductAdapter(repo);

        Product input = new Product(null, "b1", "Prod A", 10);

        var docToReturn = ProductMapper.toDoc(new Product("p1", "b1", "Prod A", 10));
        when(repo.save(any())).thenReturn(Mono.just(docToReturn));

        StepVerifier.create(adapter.save(input))
                .assertNext(saved -> {
                    assertEquals("p1", saved.id());
                    assertEquals("b1", saved.branchId());
                    assertEquals("Prod A", saved.name());
                    assertEquals(10, saved.stock());
                })
                .verifyComplete();

        verify(repo).save(any());
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findByIdAndBranchId_shouldReturnMappedDomain() {
        ReactiveProductMongoRepository repo = mock(ReactiveProductMongoRepository.class);
        MongoProductAdapter adapter = new MongoProductAdapter(repo);

        var doc = ProductMapper.toDoc(new Product("p1", "b1", "Prod 1", 5));
        when(repo.findByIdAndBranchId("p1", "b1")).thenReturn(Mono.just(doc));

        StepVerifier.create(adapter.findByIdAndBranchId("p1", "b1"))
                .assertNext(p -> {
                    assertEquals("p1", p.id());
                    assertEquals("b1", p.branchId());
                    assertEquals("Prod 1", p.name());
                    assertEquals(5, p.stock());
                })
                .verifyComplete();

        verify(repo).findByIdAndBranchId("p1", "b1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findByIdAndBranchId_shouldCompleteEmpty_whenRepoReturnsEmpty() {
        ReactiveProductMongoRepository repo = mock(ReactiveProductMongoRepository.class);
        MongoProductAdapter adapter = new MongoProductAdapter(repo);

        when(repo.findByIdAndBranchId("p404", "b1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByIdAndBranchId("p404", "b1"))
                .verifyComplete();

        verify(repo).findByIdAndBranchId("p404", "b1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void deleteByIdAndBranchId_shouldDelegate() {
        ReactiveProductMongoRepository repo = mock(ReactiveProductMongoRepository.class);
        MongoProductAdapter adapter = new MongoProductAdapter(repo);

        when(repo.deleteByIdAndBranchId("p1", "b1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteByIdAndBranchId("p1", "b1"))
                .verifyComplete();

        verify(repo).deleteByIdAndBranchId("p1", "b1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findTopByBranchIdOrderByStockDesc_shouldReturnMappedDomain() {
        ReactiveProductMongoRepository repo = mock(ReactiveProductMongoRepository.class);
        MongoProductAdapter adapter = new MongoProductAdapter(repo);

        var doc = ProductMapper.toDoc(new Product("pMax", "b1", "Top", 99));
        when(repo.findTopByBranchIdOrderByStockDesc("b1")).thenReturn(Mono.just(doc));

        StepVerifier.create(adapter.findTopByBranchIdOrderByStockDesc("b1"))
                .assertNext(p -> {
                    assertEquals("pMax", p.id());
                    assertEquals("b1", p.branchId());
                    assertEquals("Top", p.name());
                    assertEquals(99, p.stock());
                })
                .verifyComplete();

        verify(repo).findTopByBranchIdOrderByStockDesc("b1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findTopByBranchIdOrderByStockDesc_shouldCompleteEmpty_whenRepoReturnsEmpty() {
        ReactiveProductMongoRepository repo = mock(ReactiveProductMongoRepository.class);
        MongoProductAdapter adapter = new MongoProductAdapter(repo);

        when(repo.findTopByBranchIdOrderByStockDesc("b404")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findTopByBranchIdOrderByStockDesc("b404"))
                .verifyComplete();

        verify(repo).findTopByBranchIdOrderByStockDesc("b404");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findByBranchId_shouldMapEachElement() {
        ReactiveProductMongoRepository repo = mock(ReactiveProductMongoRepository.class);
        MongoProductAdapter adapter = new MongoProductAdapter(repo);

        var d1 = ProductMapper.toDoc(new Product("p1", "b1", "A", 1));
        var d2 = ProductMapper.toDoc(new Product("p2", "b1", "B", 2));
        when(repo.findByBranchId("b1")).thenReturn(Flux.just(d1, d2));

        StepVerifier.create(adapter.findByBranchId("b1"))
                .assertNext(p -> {
                    assertEquals("p1", p.id());
                    assertEquals("b1", p.branchId());
                    assertEquals("A", p.name());
                    assertEquals(1, p.stock());
                })
                .assertNext(p -> {
                    assertEquals("p2", p.id());
                    assertEquals("b1", p.branchId());
                    assertEquals("B", p.name());
                    assertEquals(2, p.stock());
                })
                .verifyComplete();

        verify(repo).findByBranchId("b1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findByBranchId_shouldCompleteEmpty_whenRepoReturnsEmpty() {
        ReactiveProductMongoRepository repo = mock(ReactiveProductMongoRepository.class);
        MongoProductAdapter adapter = new MongoProductAdapter(repo);

        when(repo.findByBranchId("bEmpty")).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByBranchId("bEmpty"))
                .verifyComplete();

        verify(repo).findByBranchId("bEmpty");
        verifyNoMoreInteractions(repo);
    }
}

