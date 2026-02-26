package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.MaxStockByBranchDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetMaxStockByBranchQuery {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public GetMaxStockByBranchQuery(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository
    ) {
        this.franchiseRepository = franchiseRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
    }

    public Flux<MaxStockByBranchDTO> execute(String franchiseId) {
        // Buscar franquicia y obtener las sucursales correspondientes
        return Lookups.requireFranchise(franchiseRepository, franchiseId)
                .flatMapMany(f -> branchRepository.findByFranchiseId(franchiseId))
                .switchIfEmpty(Mono.error(new NotFoundException("No branches for franchise: " + franchiseId)))
                .flatMap(branch ->
                        Mono.zip(
                                Mono.just(branch),
                                productRepository.findTopByBranchIdOrderByStockDesc(branch.getId())
                                        .switchIfEmpty(Mono.error(new NotFoundException("Branch has no products: " + branch.getId())))
                        ).map(t -> {
                            MaxStockByBranchDTO dto = new MaxStockByBranchDTO();
                            dto.setBranchId(t.getT1().getId());
                            dto.setBranchName(t.getT1().getName());
                            dto.setProductId(t.getT2().getId());
                            dto.setProductName(t.getT2().getName());
                            dto.setStock(t.getT2().getStock());
                            return dto;
                        })
                );
    }
}