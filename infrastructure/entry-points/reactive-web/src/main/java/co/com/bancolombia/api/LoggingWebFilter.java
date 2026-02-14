package co.com.bancolombia.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

@Component
public class LoggingWebFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(LoggingWebFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        long start = System.currentTimeMillis();

        return chain.filter(exchange)
                .doOnSubscribe(s -> log.info("onNext request start {} {}", req.getMethod(), req.getURI()))
                .doOnError(e -> log.error("onError request {} {}", req.getMethod(), req.getURI(), e))
                .doFinally(sig -> log.info("onComplete request end signal={} {} {} ms={}",
                        sig, req.getMethod(), req.getURI(), (System.currentTimeMillis() - start)));
    }
}
