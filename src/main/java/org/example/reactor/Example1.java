package org.example.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

public class Example1 {

    private static final Logger log = LoggerFactory.getLogger(Example1.class);

    public Flux<Integer> nameAndTag() {
        return Flux.range(1, 10)
                .name("intRange")
                .tag("audience", "java.IL");
    }


    public Flux<Integer> divide100By(Flux<Integer> dividers) {
        return dividers.flatMap(div ->
                Mono.just(100 / div)
                        .doOnError(e -> {
                            if (e instanceof ArithmeticException) log.warn("0 again?!", e);
                        })
                        .onErrorResume(ArithmeticException.class, e -> Mono.empty())
        );
    }

    public void hooks() {
        // last operator before subscribe
        Hooks.onLastOperator(
                Operators.lift(scannable ->
                                scannable.tags()
                                        .anyMatch(tag -> tag.getT1().contains("createdBy")),
                        (scannable, subscriber) -> {
                            log.info("{} subscribed to {}", subscriber, scannable.name());
                            return subscriber;
                        }));
    }
}
