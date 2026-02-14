package co.com.bancolombia.mongo.repository;

import co.com.bancolombia.mongo.document.BranchDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveBranchMongoRepository extends ReactiveMongoRepository<BranchDocument, String> {
    Mono<BranchDocument> findByIdAndFranchiseId(String id, String franchiseId);
    Flux<BranchDocument> findByFranchiseId(String franchiseId);
}
