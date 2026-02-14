package co.com.bancolombia.mongo.repository;

import co.com.bancolombia.mongo.document.FranchiseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveFranchiseMongoRepository extends ReactiveMongoRepository<FranchiseDocument, String> {
    Mono<Boolean> existsByName(String name);
}
