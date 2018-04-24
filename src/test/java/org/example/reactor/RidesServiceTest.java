package org.example.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class RidesServiceTest {

    @Test
    public void basic() {
        Flux<Integer> publisher = Flux.just(1, 2, 3, 4, 5);

        StepVerifier.create(publisher)
                .expectNext(1)
                .assertNext(v -> assertThat(v).isBetween(1, 3))
                .expectNextMatches(item -> item == 3)
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    @Test
    public void errors() {
        Flux<Integer> publisher = Flux.just(1, 2, 3, 4, 5)
                .concatWith(Flux.error(new IllegalStateException("wat?!")));

        StepVerifier.create(publisher)
                .expectNext(1)
                .expectNextCount(4)
                .expectErrorMessage("wat?!")
                .verify();
    }

    @Test
    public void virtualTime() {
        StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofSeconds(45)).take(2))
                .thenAwait(Duration.ofSeconds(50))
                .expectNext(0L)
                .thenAwait(Duration.ofSeconds(40))
                .assertNext(v -> assertThat(v).isGreaterThan(0L))
                .expectComplete()
                .verify();
    }

    @Test
    public void contextPropagation() {
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .subscriberContext(ctx -> ctx.put(key, "World"))
                .flatMap(s -> Mono
                        .subscriberContext()
                        .map(ctx -> s + " " +
                                ctx.getOrDefault(key, "Stranger")));

        StepVerifier.create(r)
                .expectNext("Hello Stranger")
                .verifyComplete();
    }

    @Test
    public void booooom() {
        TestPublisher.create().next(1, 2, 3, 4)
                .complete();

        TestPublisher.createNoncompliant(TestPublisher.Violation.ALLOW_NULL)
                .emit(1, 2, 3, null, 4);

    }
}