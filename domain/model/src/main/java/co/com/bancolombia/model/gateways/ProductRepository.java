package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.ProductDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<ProductDTO> save(ProductDTO product);
    Mono<ProductDTO> findByIdAndBranchId(String productId, String branchId);
    Mono<Void> deleteByIdAndBranchId(String productId, String branchId);
    Mono<ProductDTO> findTopByBranchIdOrderByStockDesc(String branchId);
    Flux<ProductDTO> findByBranchId(String branchId);
}
