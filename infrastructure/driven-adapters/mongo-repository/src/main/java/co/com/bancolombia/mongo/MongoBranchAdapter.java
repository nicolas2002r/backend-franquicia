package co.com.bancolombia.mongo;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.mongo.mapper.BranchMapper;
import co.com.bancolombia.mongo.repository.ReactiveBranchMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MongoBranchAdapter implements BranchRepository {
    private final ReactiveBranchMongoRepository repo;

    public MongoBranchAdapter(ReactiveBranchMongoRepository repo) { this.repo = repo; }

    @Override public Mono<Branch> save(Branch branch) {
        return repo.save(BranchMapper.toDoc(branch)).map(BranchMapper::toDomain);
    }

    @Override public Mono<Branch> findByIdAndFranchiseId(String branchId, String franchiseId) {
        return repo.findByIdAndFranchiseId(branchId, franchiseId).map(BranchMapper::toDomain);
    }

    @Override public Flux<Branch> findByFranchiseId(String franchiseId) {
        return repo.findByFranchiseId(franchiseId).map(BranchMapper::toDomain);
    }

    @Override public Mono<Branch> updateName(String branchId, String franchiseId, String newName) {
        return repo.findByIdAndFranchiseId(branchId, franchiseId)
                .flatMap(d -> { d.setName(newName); return repo.save(d); })
                .map(BranchMapper::toDomain);
    }
}

