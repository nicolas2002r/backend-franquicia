package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.usecase.franchise.command.UpdateBranchNameCommand;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBranchNameCommandTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private UpdateBranchNameCommand command;

    @Test
    void executeWithValidDataShouldUpdateBranch() {
        String franchiseId = "franchise123";
        String branchId = "branch456";
        String newName = "Updated Branch";
        BranchDTO updatedBranch = new BranchDTO(branchId, franchiseId, newName);

        when(branchRepository.updateName(branchId, franchiseId, newName)).thenReturn(Mono.just(updatedBranch));

        StepVerifier.create(command.execute(franchiseId, branchId, newName))
                .expectNext(updatedBranch)
                .verifyComplete();
    }

    @Test
    void executeWithNullNameShouldThrowValidationException() {
        String franchiseId = "franchise123";
        String branchId = "branch456";

        StepVerifier.create(command.execute(franchiseId, branchId, null))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals(Messages.BRANCH_NAME_REQUIRED))
                .verify();
    }

    @Test
    void executeWithEmptyNameShouldThrowValidationException() {
        String franchiseId = "franchise123";
        String branchId = "branch456";

        StepVerifier.create(command.execute(franchiseId, branchId, "   "))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals(Messages.BRANCH_NAME_REQUIRED))
                .verify();
    }

    @Test
    void executeWhenBranchNotFoundShouldThrowNotFoundException() {
        String franchiseId = "franchise123";
        String branchId = "nonexistent";
        String newName = "Updated Branch";

        when(branchRepository.updateName(branchId, franchiseId, newName)).thenReturn(Mono.empty());

        StepVerifier.create(command.execute(franchiseId, branchId, newName))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals(Messages.NO_SUCURSAL))
                .verify();
    }
}

