package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class AddBranchCommand {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;

    public AddBranchCommand(FranchiseRepository franchiseRepository, BranchRepository branchRepository) {
        this.franchiseRepository = franchiseRepository;
        this.branchRepository = branchRepository;
    }

    public Mono<BranchDTO> execute(String franchiseId, String branchName) {
        return  Validators.requiredTrimmed(branchName, Messages.BRANCH_NAME_REQUIRED)
                .flatMap(n -> Lookups.requireFranchise(franchiseRepository, franchiseId).thenReturn(n))
                .flatMap(n -> branchRepository.save(new BranchDTO(null, franchiseId, n)));

    }
}