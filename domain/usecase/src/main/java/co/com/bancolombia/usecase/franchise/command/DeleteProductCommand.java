package co.com.bancolombia.usecase.franchise.command;


import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import reactor.core.publisher.Mono;

public class DeleteProductCommand {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final AuditSupport audit;

    public DeleteProductCommand(BranchRepository branchRepository, ProductRepository productRepository, AuditSupport audit) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.audit = audit;
    }

    public Mono<Void> execute(String franchiseId, String branchId, String productId) {
        Mono<Void> flow = Lookups.requireBranch(branchRepository, franchiseId, branchId)
                .then(Lookups.requireProduct(productRepository, branchId, productId))
                .flatMap(p -> productRepository.deleteByIdAndBranchId(productId, branchId));

        return audit.voidMono("deleteProduct", flow);
    }
}
