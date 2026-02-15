package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.MaxStockByBranch;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetMaxStockByBranchQuery {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final AuditSupport audit;

    public GetMaxStockByBranchQuery(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            AuditSupport audit
    ) {
        this.franchiseRepository = franchiseRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.audit = audit;
    }

    public Flux<MaxStockByBranch> execute(String franchiseId) {
        Flux<MaxStockByBranch> flow = Lookups.requireFranchise(franchiseRepository, franchiseId)
                .flatMapMany(f -> branchRepository.findByFranchiseId(franchiseId))
                .switchIfEmpty(Flux.error(new NotFoundException("No branches for franchise: " + franchiseId)))
                .flatMap(branch ->
                        Mono.zip(
                                Mono.just(branch),
                                productRepository.findTopByBranchIdOrderByStockDesc(branch.id())
                                        .switchIfEmpty(Mono.error(new NotFoundException("Branch has no products: " + branch.id())))
                        ).map(t -> new MaxStockByBranch(
                                t.getT1().id(),
                                t.getT1().name(),
                                t.getT2().id(),
                                t.getT2().name(),
                                t.getT2().stock()
                        ))
                );

        return audit.flux("getMaxStockByBranch", flow);
    }
}

