package co.com.bancolombia.usecase.shared;

import co.com.bancolombia.model.gateways.AuditLogger;
import co.com.bancolombia.usecase.franchise.shared.AuditSupport;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditSupportTest {

    @Test
    void infoShouldDelegateToAuditLogger() {
        AuditLogger audit = mock(AuditLogger.class);
        when(audit.info("msg")).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);

        StepVerifier.create(support.info("msg")).verifyComplete();

        verify(audit).info("msg");
        verifyNoMoreInteractions(audit);
    }

    @Test
    void errorShouldDelegateToAuditLogger() {
        AuditLogger audit = mock(AuditLogger.class);
        RuntimeException ex = new RuntimeException("boom");
        when(audit.error("msg", ex)).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);

        StepVerifier.create(support.error("msg", ex)).verifyComplete();

        verify(audit).error("msg", ex);
        verifyNoMoreInteractions(audit);
    }

    @Test
    void monoShouldLogOnNextAndOnCompleteAndReturnValue() {
        AuditLogger audit = mock(AuditLogger.class);
        when(audit.info(any())).thenReturn(Mono.empty());
        when(audit.error(any(), any())).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);

        StepVerifier.create(support.mono("opX", Mono.just("OK")))
                .expectNext("OK")
                .verifyComplete();

        verify(audit).info("onNext opX");
        verify(audit).info("onComplete opX");
        verifyNoMoreInteractions(audit);
    }

    @Test
    void monoShouldLogOnErrorAndPropagateSameError() {
        AuditLogger audit = mock(AuditLogger.class);
        when(audit.info(any())).thenReturn(Mono.empty());
        when(audit.error(any(), any())).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);
        RuntimeException boom = new RuntimeException("boom");

        StepVerifier.create(support.mono("opErr", Mono.error(boom)))
                .expectErrorSatisfies(err -> assertSame(boom, err))
                .verify();

        verify(audit).error("onError opErr", boom);
        verifyNoMoreInteractions(audit);
    }

    @Test
    void voidMonoShouldLogOnCompleteWhenSourceCompletes() {
        AuditLogger audit = mock(AuditLogger.class);
        when(audit.info(any())).thenReturn(Mono.empty());
        when(audit.error(any(), any())).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);

        StepVerifier.create(support.voidMono("opVoid", Mono.empty()))
                .verifyComplete();

        verify(audit).info("onComplete opVoid");
        verifyNoMoreInteractions(audit);
    }

    @Test
    void voidMonoShouldLogOnErrorAndPropagateSameError() {
        AuditLogger audit = mock(AuditLogger.class);
        when(audit.info(any())).thenReturn(Mono.empty());
        when(audit.error(any(), any())).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);
        IllegalStateException boom = new IllegalStateException("x");

        Mono<Void> pipeline = support.voidMono("opVoidErr", Mono.error(boom));

        StepVerifier.create(pipeline)
                .expectErrorSatisfies(err -> assertSame(boom, err))
                .verify();

        verify(audit).info("onComplete opVoidErr");
        verify(audit).error("onError opVoidErr", boom);
        verifyNoMoreInteractions(audit);
    }

    @Test
    void fluxShouldLogOnNextEachItemAndLogOnCompleteAtEnd() {
        AuditLogger audit = mock(AuditLogger.class);
        when(audit.info(any())).thenReturn(Mono.empty());
        when(audit.error(any(), any())).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);

        Flux<Integer> pipeline = support.flux("opFlux", Flux.just(1, 2, 3));

        StepVerifier.create(pipeline)
                .expectNext(1, 2, 3)
                .verifyComplete();

        verify(audit, times(3)).info("onNext opFlux");
        verify(audit).info("onComplete opFlux");
        verifyNoMoreInteractions(audit);
    }

    @Test
    void fluxShouldLogOnErrorAndPropagateSameError() {
        AuditLogger audit = mock(AuditLogger.class);
        when(audit.info(any())).thenReturn(Mono.empty());
        when(audit.error(any(), any())).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);
        RuntimeException boom = new RuntimeException("boom");

        Flux<String> pipeline = support.flux("opFluxErr", Flux.error(boom));

        StepVerifier.create(pipeline)
                .expectErrorSatisfies(err -> assertSame(boom, err))
                .verify();

        verify(audit).info("onComplete opFluxErr");
        verify(audit).error("onError opFluxErr", boom);
        verifyNoMoreInteractions(audit);
    }

    @Test
    void fluxShouldLogOnNextBeforeErrorWhenEmitsThenFails() {
        AuditLogger audit = mock(AuditLogger.class);
        when(audit.info(any())).thenReturn(Mono.empty());
        when(audit.error(any(), any())).thenReturn(Mono.empty());

        AuditSupport support = new AuditSupport(audit);
        RuntimeException boom = new RuntimeException("boom");

        Flux<String> pipeline = support.flux("opMix", Flux.concat(Flux.just("A"), Flux.error(boom)));

        StepVerifier.create(pipeline)
                .expectNext("A")
                .expectErrorSatisfies(err -> assertSame(boom, err))
                .verify();

        verify(audit).info("onComplete opMix");
        verify(audit).info("onNext opMix");
        verify(audit).error("onError opMix", boom);
        verifyNoMoreInteractions(audit);
    }
}
