package org.example.reactor;

import io.vavr.collection.List;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;


public class VehiclesService {

    private final List<VehicleLocator> vehicleLocators;

    public VehiclesService(Collection<VehicleLocator> vehicleLocators) {
        this.vehicleLocators = List.ofAll(vehicleLocators);
    }

    public Flux<Vehicle> getAvailableVehicles(Location location) {
        Flux<Vehicle> nearest = locateVehicles(location, 500);

        Flux<Vehicle> further1 = Flux.defer(() -> locateVehicles(location, 1000))
                .delaySubscription(Duration.ofSeconds(5));

        Flux<Vehicle> further2 = Flux.defer(() -> locateVehicles(location, 2000)
                .delaySubscription(Duration.ofSeconds(10)));

        return nearest
                .switchIfEmpty(further1)
                .switchIfEmpty(further2)
                .retry(2)
                .timeout(Duration.ofSeconds(15), Flux.empty())
                .take(10)
                .log();
    }

    public Mono<Long> vehiclesCount(Location location, Integer radius) {
        return locateVehicles(location, radius)
                .take(Duration.ofSeconds(10))
                .reduce(0L, (count, vehicle) -> count + 1);
    }

    @NotNull
    private Flux<Vehicle> locateVehicles(Location location, Integer radius) {
        List<Flux<Vehicle>> vehiclesFluxes = vehicleLocators.take(3)
                .zipWithIndex()
                .map(pair -> {
                    VehicleLocator locator = pair._1();
                    Integer index = pair._2();
                    if (index == 0) {
                        return locator.locate(location, radius);
                    } else {
                        // fallback
                        return Flux.defer(() -> locator.locate(location, radius))
                                .delaySubscription(Duration.ofSeconds(index));
                    }
                });

        return Flux.first(vehiclesFluxes);
    }
}
