package co.com.bancolombia.usecase.franchise.command;


import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class AddBranchCommand {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final AuditSupport audit;

    public AddBranchCommand(FranchiseRepository franchiseRepository, BranchRepository branchRepository, AuditSupport audit) {
        this.franchiseRepository = franchiseRepository;
        this.branchRepository = branchRepository;
        this.audit = audit;
    }

    public Mono<Branch> execute(String franchiseId, String branchName) {
        Mono<Branch> flow = Validators.requiredTrimmed(branchName, Messages.BRANCH_NAME_REQUIRED)
                .flatMap(n -> Lookups.requireFranchise(franchiseRepository, franchiseId).thenReturn(n))
                .flatMap(n -> branchRepository.save(new Branch(null, franchiseId, n)));

        return audit.mono("addBranch", flow);
    }
}

