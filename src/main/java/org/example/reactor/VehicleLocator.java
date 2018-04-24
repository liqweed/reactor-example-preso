package org.example.reactor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

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


    public Flux<Vehicle> locate(Location location, Integer radiusInMeters) {
        return client.get()
                .uri("/vehicles?x={x}&,y={y}radius={radius}", location.getX(), location.getY(), radiusInMeters)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(Charset.forName("UTF-8"))
                .ifNoneMatch("*")
                .ifModifiedSince(ZonedDateTime.now())
                .retrieve()
                .bodyToFlux(Vehicle.class);
    }
}
