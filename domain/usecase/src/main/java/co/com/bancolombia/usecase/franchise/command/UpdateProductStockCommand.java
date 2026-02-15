package co.com.bancolombia.usecase.franchise.command;


import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class UpdateProductStockCommand {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final AuditSupport audit;

    public UpdateProductStockCommand(BranchRepository branchRepository, ProductRepository productRepository, AuditSupport audit) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.audit = audit;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String productId, int newStock) {
        Mono<Product> flow = Validators.nonNegative(newStock, Messages.STOCK_NEGATIVE)
                .then(Lookups.requireBranch(branchRepository, franchiseId, branchId))
                .then(Lookups.requireProduct(productRepository, branchId, productId))
                .map(p -> new Product(p.id(), p.branchId(), p.name(), newStock))
                .flatMap(productRepository::save);

        return audit.mono("updateProductStock", flow);
    }
}
