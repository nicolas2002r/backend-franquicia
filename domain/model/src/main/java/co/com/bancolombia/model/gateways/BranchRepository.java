package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.BranchDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {
    Mono<BranchDTO> save(BranchDTO branch);
    Mono<BranchDTO> findByIdAndFranchiseId(String branchId, String franchiseId);
    Flux<BranchDTO> findByFranchiseId(String franchiseId);
    Mono<BranchDTO> updateName(String branchId, String franchiseId, String newName);
}
