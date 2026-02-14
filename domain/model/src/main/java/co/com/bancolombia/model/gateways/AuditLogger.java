package co.com.bancolombia.model.gateways;

import reactor.core.publisher.Mono;

public interface AuditLogger {
    Mono<Void> info(String message);
    Mono<Void> error(String message, Throwable error);
}
