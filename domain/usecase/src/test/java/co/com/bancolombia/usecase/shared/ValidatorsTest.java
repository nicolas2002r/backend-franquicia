package co.com.bancolombia.usecase.shared;

import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.usecase.franchise.shared.Validators;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorsTest {

    @Test
    void requiredTrimmedShouldEmitTrimmedValueWhenValid() {
        StepVerifier.create(Validators.requiredTrimmed("  hola  ", "msg"))
                .expectNext("hola")
                .verifyComplete();
    }

    @Test
    void requiredTrimmedShouldFailWhenNull() {
        StepVerifier.create(Validators.requiredTrimmed(null, "campo requerido"))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof ValidationException);
                    assertEquals("campo requerido", ex.getMessage());
                })
                .verify();
    }

    @Test
    void requiredTrimmedShouldFailWhenBlankOrOnlySpaces() {
        StepVerifier.create(Validators.requiredTrimmed("   ", "no puede estar vacio"))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof ValidationException);
                    assertEquals("no puede estar vacio", ex.getMessage());
                })
                .verify();
    }

    @Test
    void nonNegativeShouldEmitStockWhenZeroOrPositive() {
        StepVerifier.create(Validators.nonNegative(0, "msg"))
                .expectNext(0)
                .verifyComplete();

        StepVerifier.create(Validators.nonNegative(10, "msg"))
                .expectNext(10)
                .verifyComplete();
    }

    @Test
    void nonNegativeShouldFailWhenNegative() {
        StepVerifier.create(Validators.nonNegative(-1, "stock invalido"))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof ValidationException);
                    assertEquals("stock invalido", ex.getMessage());
                })
                .verify();
    }

    @Test
    void privateConstructorShouldBeCallableViaReflectionForCoverage() throws Exception {
        Constructor<Validators> ctor = Validators.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        Validators instance = ctor.newInstance();
        assertNotNull(instance);
    }
}
