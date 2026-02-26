package co.com.bancolombia.usecase.franchise.command;

import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Mono;

public class CreateFranchiseCommand {

    private final FranchiseRepository franchiseRepository;

    public CreateFranchiseCommand(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    public Mono<FranchiseDTO> execute(String name) {
        return Validators.requiredTrimmed(name, Messages.FRANCHISE_NAME_REQUIRED)
                .flatMap(n -> franchiseRepository.existsByName(n)
                        .flatMap(exists -> exists
                                ? Mono.error(new ConflictException(Messages.FRANCHISE_NAME_EXISTS))
                                : Mono.just(n)))
                .flatMap(n -> franchiseRepository.save(new FranchiseDTO(null, n)));

    }
}