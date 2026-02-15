package co.com.bancolombia.usecase.franchise.command;


import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class AddProductCommand {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final AuditSupport audit;

    public AddProductCommand(BranchRepository branchRepository, ProductRepository productRepository, AuditSupport audit) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.audit = audit;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String productName, int stock) {
        Mono<Product> flow = Validators.nonNegative(stock, Messages.STOCK_NEGATIVE)
                .then(Validators.requiredTrimmed(productName, Messages.PRODUCT_NAME_REQUIRED))
                .flatMap(n -> Lookups.requireBranch(branchRepository, franchiseId, branchId).thenReturn(n))
                .flatMap(n -> productRepository.save(new Product(null, branchId, n, stock)));

        return audit.mono("addProduct", flow);
    }
}
