package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {
    Mono<Branch> save(Branch branch);
    Mono<Branch> findByIdAndFranchiseId(String branchId, String franchiseId);
    Flux<Branch> findByFranchiseId(String franchiseId);
    Mono<Branch> updateName(String branchId, String franchiseId, String newName);
}
