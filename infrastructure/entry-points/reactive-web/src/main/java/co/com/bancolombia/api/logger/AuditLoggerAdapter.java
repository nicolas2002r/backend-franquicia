package co.com.bancolombia.api.logger;

import co.com.bancolombia.model.gateways.AuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuditLoggerAdapter implements AuditLogger {
    private static final Logger log = LoggerFactory.getLogger(AuditLoggerAdapter.class);

    @Override
    public Mono<Void> info(String message) {
        return Mono.fromRunnable(() -> log.info(message)).then();
    }

    @Override
    public Mono<Void> error(String message, Throwable error) {
        return Mono.fromRunnable(() -> log.error(message, error)).then();
    }
}

