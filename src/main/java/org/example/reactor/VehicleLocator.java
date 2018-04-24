package org.example.reactor;

import io.vavr.collection.Stream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.Collections;

@Component
public class VehicleLocator {

    private final WebClient client;

    public VehicleLocator(String remoteAddress) {
        client = WebClient
                .builder()
                .baseUrl("https://" + remoteAddress)
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "https://" + remoteAddress))
                .build();
    }

    public Flux<Vehicle> locate(Location location, Integer radius) {
        Integer pageSize = 500;
        int maxConcurrency = 10;
        int prefetch = pageSize;

        return vehiclesCount(location, radius).flux()
                .flatMap(count -> Flux.fromIterable(Stream.rangeBy(0, count, pageSize))
                        .flatMapSequentialDelayError(offset -> locatePaged(location, radius, offset, pageSize), maxConcurrency, prefetch));
    }

    private Flux<Vehicle> locatePaged(Location location, Integer radiusInMeters, Long offset, Integer limit) {
        float x = location.getX();
        float y = location.getY();
        return client.get()
                .uri("/vehicles?x={x}&y={y}&r={radius}&limit={limit}&offset={offset}", x, y, radiusInMeters, offset, limit)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(Charset.forName("UTF-8"))
                .ifNoneMatch("*")
                .ifModifiedSince(ZonedDateTime.now())
                .retrieve()
                .bodyToFlux(Vehicle.class);
    }

    private Mono<Long> vehiclesCount(Location location, Integer radiusInMeters) {
        float x = location.getX();
        float y = location.getY();
        return client.get()
                .uri("/vehiclesCount?x={x}&y={y}&r={radius}", x, y, radiusInMeters)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(Charset.forName("UTF-8"))
                .retrieve()
                .bodyToMono(Long.class);
    }
}
