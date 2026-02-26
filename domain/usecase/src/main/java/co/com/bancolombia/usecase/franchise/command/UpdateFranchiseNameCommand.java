package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class UpdateFranchiseNameCommand {

    private final FranchiseRepository franchiseRepository;

    public UpdateFranchiseNameCommand(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    public Mono<FranchiseDTO> execute(String franchiseId, String newName) {
        // Validación del nombre y verificación de existencia
        return Validators.requiredTrimmed(newName, Messages.FRANCHISE_NAME_REQUIRED)
                .flatMap(n -> franchiseRepository.existsByName(n)
                        .flatMap(exists -> exists
                                ? Mono.error(new ConflictException(Messages.FRANCHISE_NAME_EXISTS))
                                : Mono.just(n)))
                .flatMap(n -> franchiseRepository.updateName(franchiseId, n)
                        .switchIfEmpty(Mono.error(new NotFoundException(Messages.FRANQUICIA_NO_ENCONTRADA + franchiseId))));

    }
}