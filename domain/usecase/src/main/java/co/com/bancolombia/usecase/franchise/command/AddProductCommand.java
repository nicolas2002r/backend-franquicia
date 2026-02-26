package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class AddProductCommand {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public AddProductCommand(BranchRepository branchRepository, ProductRepository productRepository) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
    }

    public Mono<ProductDTO> execute(String franchiseId, String branchId, String productName, int stock) {

        return Validators.nonNegative(stock, Messages.STOCK_NEGATIVE)
                .then(Validators.requiredTrimmed(productName, Messages.PRODUCT_NAME_REQUIRED))
                .flatMap(n -> Lookups.requireBranch(branchRepository, franchiseId, branchId).thenReturn(n))
                .flatMap(n -> productRepository.save(new ProductDTO(null, branchId, n, stock)));

    }
}