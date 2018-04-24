package org.example.reactor;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Example1Test {

    @Test
    public void context() {
        Mono<String> mono = Mono.just("Hello")
                .flatMap(s -> Mono.subscriberContext()
                        .map(ctx -> s + " " + ctx.getOrDefault("key", "Stranger")))
                .subscriberContext(ctx -> ctx.put("key", "World"));


        StepVerifier.create(mono)
                .expectNext("Hello World")
                .verifyComplete();
    }
}