package co.com.bancolombia.usecase.franchise.shared;


import co.com.bancolombia.model.gateways.AuditLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AuditSupport {

    private final AuditLogger audit;

    public AuditSupport(AuditLogger audit) {
        this.audit = audit;
    }

    public Mono<Void> info(String message) {
        return audit.info(message);
    }

    public Mono<Void> error(String message, Throwable error) {
        return audit.error(message, error);
    }

    public <T> Mono<T> mono(String op, Mono<T> source) {
        return source
                .flatMap(v -> info("onNext " + op).thenReturn(v))
                .flatMap(v -> info("onComplete " + op).thenReturn(v))
                .onErrorResume(e -> error("onError " + op, e).then(Mono.error(e)));
    }

    public Mono<Void> voidMono(String op, Mono<Void> source) {
        return source
                .then(info("onComplete " + op))
                .onErrorResume(e -> error("onError " + op, e).then(Mono.error(e)));
    }

    public <T> Flux<T> flux(String op, Flux<T> source) {
        return source
                .flatMap(v -> info("onNext " + op).thenReturn(v))
                .onErrorResume(e -> error("onError " + op, e).thenMany(Flux.error(e)))
                .concatWith(info("onComplete " + op).thenMany(Flux.empty()));
    }
}
