package co.com.bancolombia.mongo;

import co.com.bancolombia.model.Franchise;
import co.com.bancolombia.mongo.mapper.FranchiseMapper;
import co.com.bancolombia.mongo.repository.ReactiveFranchiseMongoRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MongoFranchiseAdapterTest {

    private static Object invoke(Object target, String method, Class<?>[] paramTypes, Object... args) {
        try {
            var m = target.getClass().getMethod(method, paramTypes);
            return m.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Reflection invoke failed for " + method + " on " + target.getClass(), e);
        }
    }

    private static Object invoke(Object target, String method) {
        return invoke(target, method, new Class<?>[]{});
    }

    private static String getString(Object target, String getterName) {
        Object v = invoke(target, getterName);
        return v == null ? null : String.valueOf(v);
    }

    @Test
    void save_shouldMapToDoc_andBackToDomain() {
        ReactiveFranchiseMongoRepository repo = mock(ReactiveFranchiseMongoRepository.class);
        MongoFranchiseAdapter adapter = new MongoFranchiseAdapter(repo);

        Franchise input = new Franchise(null, "Franquicia A");

        var docToReturn = FranchiseMapper.toDoc(new Franchise("f1", "Franquicia A"));
        when(repo.save(any())).thenReturn(Mono.just(docToReturn));

        StepVerifier.create(adapter.save(input))
                .assertNext(saved -> {
                    assertEquals("f1", saved.id());
                    assertEquals("Franquicia A", saved.name());
                })
                .verifyComplete();

        verify(repo).save(any());
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findById_shouldReturnMappedDomain() {
        ReactiveFranchiseMongoRepository repo = mock(ReactiveFranchiseMongoRepository.class);
        MongoFranchiseAdapter adapter = new MongoFranchiseAdapter(repo);

        var doc = FranchiseMapper.toDoc(new Franchise("f1", "F1"));
        when(repo.findById("f1")).thenReturn(Mono.just(doc));

        StepVerifier.create(adapter.findById("f1"))
                .assertNext(f -> {
                    assertEquals("f1", f.id());
                    assertEquals("F1", f.name());
                })
                .verifyComplete();

        verify(repo).findById("f1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findById_shouldCompleteEmpty_whenRepoReturnsEmpty() {
        ReactiveFranchiseMongoRepository repo = mock(ReactiveFranchiseMongoRepository.class);
        MongoFranchiseAdapter adapter = new MongoFranchiseAdapter(repo);

        when(repo.findById("f404")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("f404"))
                .verifyComplete();

        verify(repo).findById("f404");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void existsByName_shouldDelegate() {
        ReactiveFranchiseMongoRepository repo = mock(ReactiveFranchiseMongoRepository.class);
        MongoFranchiseAdapter adapter = new MongoFranchiseAdapter(repo);

        when(repo.existsByName("X")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByName("X"))
                .expectNext(true)
                .verifyComplete();

        verify(repo).existsByName("X");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void updateName_shouldLoadDoc_setName_save_andReturnMappedDomain() {
        ReactiveFranchiseMongoRepository repo = mock(ReactiveFranchiseMongoRepository.class);
        MongoFranchiseAdapter adapter = new MongoFranchiseAdapter(repo);

        var existingDoc = FranchiseMapper.toDoc(new Franchise("f1", "Old"));
        when(repo.findById("f1")).thenReturn(Mono.just(existingDoc));

        when(repo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(adapter.updateName("f1", "NewName"))
                .assertNext(updated -> {
                    assertEquals("f1", updated.id());
                    assertEquals("NewName", updated.name());
                })
                .verifyComplete();

        verify(repo).findById("f1");
        verify(repo).save(argThat(savedDoc -> "NewName".equals(getString(savedDoc, "getName"))));
        verifyNoMoreInteractions(repo);
    }

    @Test
    void updateName_shouldCompleteEmpty_whenNotFound_andNotSave() {
        ReactiveFranchiseMongoRepository repo = mock(ReactiveFranchiseMongoRepository.class);
        MongoFranchiseAdapter adapter = new MongoFranchiseAdapter(repo);

        when(repo.findById("f404")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.updateName("f404", "X"))
                .verifyComplete();

        verify(repo).findById("f404");
        verify(repo, never()).save(any());
        verifyNoMoreInteractions(repo);
    }
}

