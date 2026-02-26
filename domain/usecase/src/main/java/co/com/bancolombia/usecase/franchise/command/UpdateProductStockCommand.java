package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class UpdateProductStockCommand {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public UpdateProductStockCommand(BranchRepository branchRepository, ProductRepository productRepository) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
    }

    public Mono<ProductDTO> execute(String franchiseId, String branchId, String productId, int newStock) {
        // Validación del nuevo stock y actualización del producto
        return Validators.nonNegative(newStock, Messages.STOCK_NEGATIVE)
                .then(Lookups.requireBranch(branchRepository, franchiseId, branchId))
                .then(Lookups.requireProduct(productRepository, branchId, productId))
                .map(p -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(p.getId());
                    productDTO.setBranchId(p.getBranchId());
                    productDTO.setName(p.getName());
                    productDTO.setStock(newStock);
                    return productDTO;
                })
                .flatMap(productRepository::save);
    }
}