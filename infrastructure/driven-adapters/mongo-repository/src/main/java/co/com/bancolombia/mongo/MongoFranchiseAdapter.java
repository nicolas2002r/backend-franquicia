package co.com.bancolombia.mongo;

import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.mongo.mapper.FranchiseMapper;
import co.com.bancolombia.mongo.repository.ReactiveFranchiseMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MongoFranchiseAdapter implements FranchiseRepository {
    private final ReactiveFranchiseMongoRepository repo;

    public MongoFranchiseAdapter(ReactiveFranchiseMongoRepository repo) { this.repo = repo; }

    @Override public Mono<Franchise> save(Franchise franchise) {
        return repo.save(FranchiseMapper.toDoc(franchise)).map(FranchiseMapper::toDomain);
    }

    @Override public Mono<Franchise> findById(String id) {
        return repo.findById(id).map(FranchiseMapper::toDomain);
    }

    @Override public Mono<Boolean> existsByName(String name) {
        return repo.existsByName(name);
    }

    @Override public Mono<Franchise> updateName(String id, String newName) {
        return repo.findById(id)
                .flatMap(d -> { d.setName(newName); return repo.save(d); })
                .map(FranchiseMapper::toDomain);
    }
}

