package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {
    Mono<Franchise> save(Franchise franchise);
    Mono<Franchise> findById(String id);
    Mono<Boolean> existsByName(String name);
    Mono<Franchise> updateName(String id, String newName);
}
