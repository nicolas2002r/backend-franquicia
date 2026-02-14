package co.com.bancolombia.mongo.repository;


import co.com.bancolombia.mongo.document.ProductDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveProductMongoRepository extends ReactiveMongoRepository<ProductDocument, String> {
    Mono<ProductDocument> findByIdAndBranchId(String id, String branchId);
    Mono<Void> deleteByIdAndBranchId(String id, String branchId);
    Mono<ProductDocument> findTopByBranchIdOrderByStockDesc(String branchId);
    Flux<ProductDocument> findByBranchId(String branchId);
}
