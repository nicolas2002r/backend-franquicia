package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class UpdateBranchNameCommand {

    private final BranchRepository branchRepository;

    public UpdateBranchNameCommand(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public Mono<BranchDTO> execute(String franchiseId, String branchId, String newName) {
        // Validación del nuevo nombre de la sucursal y actualización
        return  Validators.requiredTrimmed(newName, Messages.BRANCH_NAME_REQUIRED)
                .flatMap(n -> branchRepository.updateName(branchId, franchiseId, n)
                        .switchIfEmpty(Mono.error(new NotFoundException(Messages.NO_SUCURSAL))));

    }
}