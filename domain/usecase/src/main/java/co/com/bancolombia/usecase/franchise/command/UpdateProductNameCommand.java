package co.com.bancolombia.usecase.franchise.command;


import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class UpdateProductNameCommand {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final AuditSupport audit;

    public UpdateProductNameCommand(BranchRepository branchRepository, ProductRepository productRepository, AuditSupport audit) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.audit = audit;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String productId, String newName) {
        Mono<Product> flow = Validators.requiredTrimmed(newName, Messages.PRODUCT_NAME_REQUIRED)
                .flatMap(n -> Lookups.requireBranch(branchRepository, franchiseId, branchId).thenReturn(n))
                .flatMap(n -> Lookups.requireProduct(productRepository, branchId, productId)
                        .map(p -> new Product(p.id(), p.branchId(), n, p.stock())))
                .flatMap(productRepository::save);

        return audit.mono("updateProductName", flow);
    }
}
