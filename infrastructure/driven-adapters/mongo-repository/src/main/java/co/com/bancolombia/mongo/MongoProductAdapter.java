package co.com.bancolombia.mongo;


import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.mongo.mapper.ProductMapper;
import co.com.bancolombia.mongo.repository.ReactiveProductMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MongoProductAdapter implements ProductRepository {
    private final ReactiveProductMongoRepository repo;

    public MongoProductAdapter(ReactiveProductMongoRepository repo) { this.repo = repo; }

    @Override public Mono<Product> save(Product product) {
        return repo.save(ProductMapper.toDoc(product)).map(ProductMapper::toDomain);
    }

    @Override public Mono<Product> findByIdAndBranchId(String productId, String branchId) {
        return repo.findByIdAndBranchId(productId, branchId).map(ProductMapper::toDomain);
    }

    @Override public Mono<Void> deleteByIdAndBranchId(String productId, String branchId) {
        return repo.deleteByIdAndBranchId(productId, branchId);
    }

    @Override public Mono<Product> findTopByBranchIdOrderByStockDesc(String branchId) {
        return repo.findTopByBranchIdOrderByStockDesc(branchId).map(ProductMapper::toDomain);
    }

    @Override public Flux<Product> findByBranchId(String branchId) {
        return repo.findByBranchId(branchId).map(ProductMapper::toDomain);
    }
}

