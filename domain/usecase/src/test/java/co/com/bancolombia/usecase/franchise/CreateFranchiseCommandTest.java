package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.command.CreateFranchiseCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFranchiseCommandTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private CreateFranchiseCommand createFranchiseCommand;

    @Test
    void executeWithValidNameShouldSaveFranchise() {
        String name = "New Franchise";
        FranchiseDTO savedFranchise = new FranchiseDTO("franchise123", name);

        when(franchiseRepository.existsByName(name)).thenReturn(Mono.just(false));
        when(franchiseRepository.save(any(FranchiseDTO.class))).thenReturn(Mono.just(savedFranchise));

        Mono<FranchiseDTO> result = createFranchiseCommand.execute(name);

        StepVerifier.create(result)
                .expectNext(savedFranchise)
                .verifyComplete();
    }

    @Test
    void executeWithNullNameShouldReturnError() {
        Mono<FranchiseDTO> result = createFranchiseCommand.execute(null);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Franchise name is required"))
                .verify();
    }

    @Test
    void executeWithEmptyNameShouldReturnError() {
        Mono<FranchiseDTO> result = createFranchiseCommand.execute("   ");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Franchise name is required"))
                .verify();
    }

    @Test
    void executeWithExistingNameShouldReturnConflictError() {
        String name = "Existing Franchise";

        when(franchiseRepository.existsByName(name)).thenReturn(Mono.just(true));

        Mono<FranchiseDTO> result = createFranchiseCommand.execute(name);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ConflictException &&
                        throwable.getMessage().equals("Franchise name already exists"))
                .verify();
    }
}
