package org.example.reactor;

import reactor.core.publisher.Flux;

public class VehicleLocator {

    public Flux<Vehicle> locate(Location location, Integer radiusInMeters) {
        return Flux.empty();
    }
}
