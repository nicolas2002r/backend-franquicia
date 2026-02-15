package co.com.bancolombia.usecase.franchise.command;


import co.com.bancolombia.model.Branch;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class UpdateBranchNameCommand {

    private final BranchRepository branchRepository;
    private final AuditSupport audit;

    public UpdateBranchNameCommand(BranchRepository branchRepository, AuditSupport audit) {
        this.branchRepository = branchRepository;
        this.audit = audit;
    }

    public Mono<Branch> execute(String franchiseId, String branchId, String newName) {
        Mono<Branch> flow = Validators.requiredTrimmed(newName, Messages.BRANCH_NAME_REQUIRED)
                .flatMap(n -> branchRepository.updateName(branchId, franchiseId, n)
                        .switchIfEmpty(Mono.error(new NotFoundException(Messages.NO_SUCURSAL))));

        return audit.mono("updateBranchName", flow);
    }
}
