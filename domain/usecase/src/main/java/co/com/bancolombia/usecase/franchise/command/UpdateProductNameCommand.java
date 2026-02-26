package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class UpdateProductNameCommand {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public UpdateProductNameCommand(BranchRepository branchRepository, ProductRepository productRepository) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
    }

    public Mono<ProductDTO> execute(String franchiseId, String branchId, String productId, String newName) {
        // Validación del nuevo nombre y actualización del producto
        return Validators.requiredTrimmed(newName, Messages.PRODUCT_NAME_REQUIRED)
                .flatMap(n -> Lookups.requireBranch(branchRepository, franchiseId, branchId).thenReturn(n))
                .flatMap(n -> Lookups.requireProduct(productRepository, branchId, productId)
                        .map(p -> {
                            ProductDTO productDTO = new ProductDTO();
                            productDTO.setId(p.getId());
                            productDTO.setBranchId(p.getBranchId());
                            productDTO.setName(n);
                            productDTO.setStock(p.getStock());
                            return productDTO;
                        })
                )
                .flatMap(productRepository::save);

    }
}