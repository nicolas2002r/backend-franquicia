package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.FranchiseDTO;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {
    Mono<FranchiseDTO> save(FranchiseDTO franchise);
    Mono<FranchiseDTO> findById(String id);
    Mono<Boolean> existsByName(String name);
    Mono<FranchiseDTO> updateName(String id, String newName);
}
