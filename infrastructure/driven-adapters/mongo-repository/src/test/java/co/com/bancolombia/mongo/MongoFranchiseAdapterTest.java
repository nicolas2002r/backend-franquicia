package co.com.bancolombia.mongo;


import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.mongo.document.FranchiseDocument;
import co.com.bancolombia.mongo.repository.ReactiveFranchiseMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoFranchiseAdapterTest {

    @Mock
    private ReactiveFranchiseMongoRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MongoFranchiseAdapter adapter;

    @Test
    void saveShouldReturnSavedFranchise() {
        FranchiseDTO dto = new FranchiseDTO("f1", "Test Franchise");
        FranchiseDocument doc = new FranchiseDocument("f1", "Test Franchise");

        when(objectMapper.map(dto, FranchiseDocument.class)).thenReturn(doc);
        when(repository.save(doc)).thenReturn(Mono.just(doc));
        when(objectMapper.map(doc, FranchiseDTO.class)).thenReturn(dto);

        StepVerifier.create(adapter.save(dto))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void findByIdShouldReturnFranchiseWhenFound() {
        String id = "f1";
        FranchiseDocument doc = new FranchiseDocument(id, "Test");
        FranchiseDTO dto = new FranchiseDTO(id, "Test");

        when(repository.findById(id)).thenReturn(Mono.just(doc));
        when(objectMapper.map(doc, FranchiseDTO.class)).thenReturn(dto);

        StepVerifier.create(adapter.findById(id))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void findByIdShouldReturnEmptyWhenNotFound() {
        String id = "f1";

        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(id))
                .verifyComplete();
    }

    @Test
    void existsByNameShouldReturnTrueWhenExists() {
        String name = "Test";
        when(repository.existsByName(name)).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByName(name))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByNameShouldReturnFalseWhenNotExists() {
        String name = "Test";
        when(repository.existsByName(name)).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.existsByName(name))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void updateNameShouldUpdateAndReturnUpdatedFranchise() {
        String id = "f1";
        String newName = "New Name";
        FranchiseDocument existingDoc = new FranchiseDocument(id, "Old");
        FranchiseDocument updatedDoc = new FranchiseDocument(id, newName);

        when(repository.findById(id)).thenReturn(Mono.just(existingDoc));
        when(repository.save(any(FranchiseDocument.class))).thenReturn(Mono.just(updatedDoc));

        StepVerifier.create(adapter.updateName(id, newName))
                .expectNextMatches(dto ->
                        dto.getId().equals(id) &&
                                dto.getName().equals(newName))
                .verifyComplete();
    }

    @Test
    void updateNameShouldReturnEmptyWhenFranchiseNotFound() {
        String id = "f1";
        String newName = "New Name";

        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.updateName(id, newName))
                .verifyComplete();
    }
}

