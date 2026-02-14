package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<Product> save(Product product);
    Mono<Product> findByIdAndBranchId(String productId, String branchId);
    Mono<Void> deleteByIdAndBranchId(String productId, String branchId);
    Mono<Product> findTopByBranchIdOrderByStockDesc(String branchId);
    Flux<Product> findByBranchId(String branchId);
}
