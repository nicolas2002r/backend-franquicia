package co.com.bancolombia.usecase.shared;

import co.com.bancolombia.model.BranchDTO;
import co.com.bancolombia.model.FranchiseDTO;
import co.com.bancolombia.model.ProductDTO;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.gateways.BranchRepository;
import co.com.bancolombia.model.gateways.FranchiseRepository;
import co.com.bancolombia.model.gateways.ProductRepository;
import co.com.bancolombia.usecase.franchise.shared.Lookups;
import co.com.bancolombia.usecase.franchise.shared.Messages;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LookupsTest {

    @Test
    void requireFranchiseShouldReturnFranchiseWhenFound() {
        FranchiseRepository repo = mock(FranchiseRepository.class);
        FranchiseDTO franchise = new FranchiseDTO("f1", "Franquicia A");

        when(repo.findById("f1")).thenReturn(Mono.just(franchise));

        StepVerifier.create(Lookups.requireFranchise(repo, "f1"))
                .expectNext(franchise)
                .verifyComplete();

        verify(repo).findById("f1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void requireFranchiseShouldErrorNotFoundWhenEmpty() {
        FranchiseRepository repo = mock(FranchiseRepository.class);
        when(repo.findById("f404")).thenReturn(Mono.empty());

        StepVerifier.create(Lookups.requireFranchise(repo, "f404"))
                .expectErrorSatisfies(err -> {
                    assertInstanceOf(NotFoundException.class, err);
                    assertEquals(Messages.FRANQUICIA_NO_ENCONTRADA + "f404", err.getMessage());
                })
                .verify();

        verify(repo).findById("f404");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void requireBranchShouldReturnBranchWhenFound() {
        BranchRepository repo = mock(BranchRepository.class);
        BranchDTO branch = new BranchDTO("b1", "f1", "Sucursal 1");

        when(repo.findByIdAndFranchiseId("b1", "f1")).thenReturn(Mono.just(branch));

        StepVerifier.create(Lookups.requireBranch(repo, "f1", "b1"))
                .expectNext(branch)
                .verifyComplete();

        verify(repo).findByIdAndFranchiseId("b1", "f1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void requireBranchShouldErrorNotFoundWhenEmpty() {
        BranchRepository repo = mock(BranchRepository.class);
        when(repo.findByIdAndFranchiseId("b404", "f1")).thenReturn(Mono.empty());

        StepVerifier.create(Lookups.requireBranch(repo, "f1", "b404"))
                .expectErrorSatisfies(err -> {
                    assertInstanceOf(NotFoundException.class, err);
                    assertEquals(Messages.NO_SUCURSAL, err.getMessage());
                })
                .verify();

        verify(repo).findByIdAndFranchiseId("b404", "f1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void requireProductShouldReturnProductWhenFound() {
        ProductRepository repo = mock(ProductRepository.class);
        ProductDTO product = new ProductDTO("p1", "b1", "Producto 1", 10);

        when(repo.findByIdAndBranchId("p1", "b1")).thenReturn(Mono.just(product));

        StepVerifier.create(Lookups.requireProduct(repo, "b1", "p1"))
                .expectNext(product)
                .verifyComplete();

        verify(repo).findByIdAndBranchId("p1", "b1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void requireProductShouldErrorNotFoundWhenEmpty() {
        ProductRepository repo = mock(ProductRepository.class);
        when(repo.findByIdAndBranchId("p404", "b1")).thenReturn(Mono.empty());

        StepVerifier.create(Lookups.requireProduct(repo, "b1", "p404"))
                .expectErrorSatisfies(err -> {
                    assertInstanceOf(NotFoundException.class, err);
                    assertEquals(Messages.PRODUCTO_NO_ENCONTRADO, err.getMessage());
                })
                .verify();

        verify(repo).findByIdAndBranchId("p404", "b1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void shouldAllowInstantiationViaReflectionWhenConstructorDoesNotThrow() throws Exception {
        Constructor<Lookups> ctor = Lookups.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        Lookups instance = ctor.newInstance();

        assertNotNull(instance);
    }
}

