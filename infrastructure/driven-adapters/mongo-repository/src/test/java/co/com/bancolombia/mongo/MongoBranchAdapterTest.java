package co.com.bancolombia.mongo;

import co.com.bancolombia.model.Branch;
import co.com.bancolombia.mongo.mapper.BranchMapper;
import co.com.bancolombia.mongo.repository.ReactiveBranchMongoRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MongoBranchAdapterTest {

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
        ReactiveBranchMongoRepository repo = mock(ReactiveBranchMongoRepository.class);
        MongoBranchAdapter adapter = new MongoBranchAdapter(repo);

        Branch input = new Branch(null, "f1", "Sucursal A");

        var docToReturn = BranchMapper.toDoc(new Branch("b1", "f1", "Sucursal A"));
        when(repo.save(any())).thenReturn(Mono.just(docToReturn));

        StepVerifier.create(adapter.save(input))
                .assertNext(saved -> {
                    assertEquals("b1", saved.id());
                    assertEquals("f1", saved.franchiseId());
                    assertEquals("Sucursal A", saved.name());
                })
                .verifyComplete();

        verify(repo).save(any());
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findByIdAndFranchiseId_shouldReturnMappedDomain() {
        ReactiveBranchMongoRepository repo = mock(ReactiveBranchMongoRepository.class);
        MongoBranchAdapter adapter = new MongoBranchAdapter(repo);

        var doc = BranchMapper.toDoc(new Branch("b1", "f1", "Sucursal 1"));
        when(repo.findByIdAndFranchiseId("b1", "f1")).thenReturn(Mono.just(doc));

        StepVerifier.create(adapter.findByIdAndFranchiseId("b1", "f1"))
                .assertNext(b -> {
                    assertEquals("b1", b.id());
                    assertEquals("f1", b.franchiseId());
                    assertEquals("Sucursal 1", b.name());
                })
                .verifyComplete();

        verify(repo).findByIdAndFranchiseId("b1", "f1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void findByFranchiseId_shouldReturnFluxMappedDomain() {
        ReactiveBranchMongoRepository repo = mock(ReactiveBranchMongoRepository.class);
        MongoBranchAdapter adapter = new MongoBranchAdapter(repo);

        var doc1 = BranchMapper.toDoc(new Branch("b1", "f1", "S1"));
        var doc2 = BranchMapper.toDoc(new Branch("b2", "f1", "S2"));

        when(repo.findByFranchiseId("f1")).thenReturn(Flux.just(doc1, doc2));

        StepVerifier.create(adapter.findByFranchiseId("f1"))
                .assertNext(b -> {
                    assertEquals("b1", b.id());
                    assertEquals("f1", b.franchiseId());
                    assertEquals("S1", b.name());
                })
                .assertNext(b -> {
                    assertEquals("b2", b.id());
                    assertEquals("f1", b.franchiseId());
                    assertEquals("S2", b.name());
                })
                .verifyComplete();

        verify(repo).findByFranchiseId("f1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void updateName_shouldLoadDoc_setName_save_andReturnMappedDomain() {
        ReactiveBranchMongoRepository repo = mock(ReactiveBranchMongoRepository.class);
        MongoBranchAdapter adapter = new MongoBranchAdapter(repo);

        var existingDoc = BranchMapper.toDoc(new Branch("b1", "f1", "Old"));
        when(repo.findByIdAndFranchiseId("b1", "f1")).thenReturn(Mono.just(existingDoc));

        when(repo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(adapter.updateName("b1", "f1", "NewName"))
                .assertNext(updated -> {
                    assertEquals("b1", updated.id());
                    assertEquals("f1", updated.franchiseId());
                    assertEquals("NewName", updated.name());
                })
                .verifyComplete();

        verify(repo).findByIdAndFranchiseId("b1", "f1");
        verify(repo).save(argThat(savedDoc -> {
            String name = getString(savedDoc, "getName");
            return "NewName".equals(name);
        }));
        verifyNoMoreInteractions(repo);
    }

    @Test
    void updateName_shouldCompleteEmpty_whenNotFound_andNotSave() {
        ReactiveBranchMongoRepository repo = mock(ReactiveBranchMongoRepository.class);
        MongoBranchAdapter adapter = new MongoBranchAdapter(repo);

        when(repo.findByIdAndFranchiseId("b404", "f1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.updateName("b404", "f1", "X"))
                .verifyComplete();

        verify(repo).findByIdAndFranchiseId("b404", "f1");
        verify(repo, never()).save(any());
        verifyNoMoreInteractions(repo);
    }
}

