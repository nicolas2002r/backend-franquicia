package co.com.bancolombia.usecase.franchise.shared;


import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.Product;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import reactor.core.publisher.Mono;

public final class Lookups {
    private Lookups() {}

    public static Mono<Franchise> requireFranchise(FranchiseRepository repo, String franchiseId) {
        return repo.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException(Messages.FRANQUICIA_NO_ENCONTRADA + franchiseId)));
    }

    public static Mono<Branch> requireBranch(BranchRepository repo, String franchiseId, String branchId) {
        return repo.findByIdAndFranchiseId(branchId, franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException(Messages.NO_SUCURSAL)));
    }

    public static Mono<Product> requireProduct(ProductRepository repo, String branchId, String productId) {
        return repo.findByIdAndBranchId(productId, branchId)
                .switchIfEmpty(Mono.error(new NotFoundException(Messages.PRODUCTO_NO_ENCONTRADO)));
    }
}

