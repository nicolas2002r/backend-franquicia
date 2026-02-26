package co.com.bancolombia.usecase.franchise.shared;


import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import reactor.core.publisher.Mono;

public final class Lookups {
    private Lookups() {}

    public static Mono<FranchiseDTO> requireFranchise(FranchiseRepository repo, String franchiseId) {
        return repo.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException(Messages.FRANQUICIA_NO_ENCONTRADA + franchiseId)));
    }

    public static Mono<BranchDTO> requireBranch(BranchRepository repo, String franchiseId, String branchId) {
        return repo.findByIdAndFranchiseId(branchId, franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException(Messages.NO_SUCURSAL)));
    }

    public static Mono<ProductDTO> requireProduct(ProductRepository repo, String branchId, String productId) {
        return repo.findByIdAndBranchId(productId, branchId)
                .switchIfEmpty(Mono.error(new NotFoundException(Messages.PRODUCTO_NO_ENCONTRADO)));
    }
}

