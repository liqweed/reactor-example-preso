package org.example.reactor;

import io.vavr.collection.List;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;

import static org.mockito.Mockito.*;

public class VehiclesServiceApiTest {

    private final VehicleLocator locator1 = mock(VehicleLocator.class);
    private final VehicleLocator locator2 = mock(VehicleLocator.class);

    private final VehiclesService service = new VehiclesService(List.of(locator1, locator2).asJava());

    private final Location location = new Location(42, 31);
    private final Vehicle v1 = new Vehicle("v1");
    private final Vehicle v2 = new Vehicle("v2");
    private final Vehicle v3 = new Vehicle("v3");

    @Test
    public void takeFirst() {
        when(locator1.locate(location, 500))
                .thenReturn(Flux.just(v1));

        StepVerifier.withVirtualTime(() -> service.getAvailableVehicles(location))
                .expectNext(v1)
                .verifyComplete();
    }

    @Test
    public void retryOnError() {
        when(locator1.locate(location, 500))
                .thenThrow(new IllegalStateException("Boom!"));
        reset(locator1);
        when(locator1.locate(location, 500))
                .thenReturn(Flux.just(v1));

        StepVerifier.withVirtualTime(() -> service.getAvailableVehicles(location))
                .expectNext(v1)
                .verifyComplete();
    }

    @Test
    public void take2ndBest() {
        when(locator1.locate(location, 500))
                .thenReturn(Flux.empty());
        when(locator1.locate(location, 1000))
                .thenReturn(Flux.just(v2, v3));

        StepVerifier.withVirtualTime(() -> service.getAvailableVehicles(location))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(5))
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(v2, v3)
                .verifyComplete();
    }

    @Test
    public void fallbackToLocator2() {
        when(locator1.locate(location, 500))
                .thenReturn(Flux.just(v1).delaySequence(Duration.ofSeconds(2), VirtualTimeScheduler.getOrSet()));

        when(locator2.locate(location, 500))
                .thenReturn(Flux.just(v2, v3));

        StepVerifier.withVirtualTime(() -> service.getAvailableVehicles(location), VirtualTimeScheduler::getOrSet, 5)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .thenAwait(Duration.ofMillis(1))
                .expectNext(v2, v3)
                .verifyComplete();
    }
}