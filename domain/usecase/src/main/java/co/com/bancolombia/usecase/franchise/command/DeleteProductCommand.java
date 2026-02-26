package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import reactor.core.publisher.Mono;

public class DeleteProductCommand {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public DeleteProductCommand(BranchRepository branchRepository, ProductRepository productRepository) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
    }

    public Mono<Void> execute(String franchiseId, String branchId, String productId) {
        return Lookups.requireBranch(branchRepository, franchiseId, branchId)
                .then(Lookups.requireProduct(productRepository, branchId, productId))
                .flatMap(p -> productRepository.deleteByIdAndBranchId(productId, branchId));

    }
}