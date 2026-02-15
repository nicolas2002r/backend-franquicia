package co.com.bancolombia.usecase.franchise.shared;

import co.com.bancolombia.model.exception.ValidationException;
import reactor.core.publisher.Mono;

public final class Validators {
    private Validators() {}

    public static Mono<String> requiredTrimmed(String value, String message) {
        return Mono.justOrEmpty(value)
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .switchIfEmpty(Mono.error(new ValidationException(message)));
    }

    public static Mono<Integer> nonNegative(int stock, String message) {
        return stock < 0
                ? Mono.error(new ValidationException(message))
                : Mono.just(stock);
    }
}

