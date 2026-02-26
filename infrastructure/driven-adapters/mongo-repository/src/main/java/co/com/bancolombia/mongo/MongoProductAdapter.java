package co.com.bancolombia.mongo;

import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.mongo.document.ProductDocument;
import co.com.bancolombia.mongo.helper.AdapterOperations;
import co.com.bancolombia.mongo.repository.ReactiveProductMongoRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MongoProductAdapter extends AdapterOperations<ProductDTO, ProductDocument, String, ReactiveProductMongoRepository> implements ProductRepository {

    public MongoProductAdapter(ReactiveProductMongoRepository repo, ObjectMapper objectMapper) {
        super(repo, objectMapper, d -> objectMapper.map(d, ProductDTO.class));
    }

    @Override
    public Mono<ProductDTO> save(ProductDTO product) {
        return super.save(product);
    }

    @Override
    public Mono<ProductDTO> findByIdAndBranchId(String productId, String branchId) {
        return doQuery(repository.findByIdAndBranchId(productId, branchId));
    }

    @Override
    public Mono<Void> deleteByIdAndBranchId(String productId, String branchId) {
        return repository.deleteByIdAndBranchId(productId, branchId);
    }

    @Override
    public Mono<ProductDTO> findTopByBranchIdOrderByStockDesc(String branchId) {
        return doQuery(repository.findTopByBranchIdOrderByStockDesc(branchId));
    }

    @Override
    public Flux<ProductDTO> findByBranchId(String branchId) {
        return doQueryMany(repository.findByBranchId(branchId));
    }
}