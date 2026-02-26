package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.usecase.franchise.command.AddBranchCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddBranchCommandTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private AddBranchCommand addBranchCommand;

    @Test
    void executeWithValidDataShouldSaveBranch() {
        String franchiseId = "franchise123";
        String branchName = "Branch Center";
        BranchDTO savedBranch = new BranchDTO("branch456", franchiseId, branchName);

        FranchiseDTO franchiseMock = mock(FranchiseDTO.class);
        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchiseMock));
        when(branchRepository.save(any(BranchDTO.class))).thenReturn(Mono.just(savedBranch));

        Mono<BranchDTO> result = addBranchCommand.execute(franchiseId, branchName);

        StepVerifier.create(result)
                .expectNext(savedBranch)
                .verifyComplete();
    }

    @Test
    void executeWithNullBranchNameShouldReturnError() {
        String franchiseId = "franchise123";

        Mono<BranchDTO> result = addBranchCommand.execute(franchiseId, null);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Branch name is required"))
                .verify();
    }

    @Test
    void executeWithEmptyBranchNameShouldReturnError() {
        String franchiseId = "franchise123";

        Mono<BranchDTO> result = addBranchCommand.execute(franchiseId, "   ");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Branch name is required"))
                .verify();
    }

    @Test
    void executeWithNonExistentFranchiseShouldReturnError() {
        String franchiseId = "nonexistent";
        String branchName = "Branch Center";

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        Mono<BranchDTO> result = addBranchCommand.execute(franchiseId, branchName);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Franquicia no encontrada: nonexistent"))
                .verify();
    }
}