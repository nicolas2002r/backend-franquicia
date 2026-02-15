package co.com.bancolombia.usecase.franchise.command;


import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CreateFranchiseCommand {

    private final FranchiseRepository franchiseRepository;
    private final AuditSupport audit;

    public CreateFranchiseCommand(FranchiseRepository franchiseRepository, AuditSupport audit) {
        this.franchiseRepository = franchiseRepository;
        this.audit = audit;
    }

    public Mono<Franchise> execute(String name) {
        Mono<Franchise> flow = Validators.requiredTrimmed(name, Messages.FRANCHISE_NAME_REQUIRED)
                .flatMap(n -> franchiseRepository.existsByName(n)
                        .flatMap(exists -> exists
                                ? Mono.error(new ConflictException(Messages.FRANCHISE_NAME_EXISTS))
                                : Mono.just(n)))
                .flatMap(n -> {
                    Mono<Franchise> save = franchiseRepository.save(new Franchise(null, n));
                    Mono<Void> auditEvent = audit.info("AUDIT createFranchise name=" + n);

                    return Flux.merge(save, auditEvent.then(Mono.empty()))
                            .next()
                            .cast(Franchise.class);
                });

        return audit.mono("createFranchise", flow);
    }
}

