package org.example.reactor.rides;

import org.example.reactor.Reservation;
import org.example.reactor.RideOffer;
import org.example.reactor.RidesHandlingException;
import org.example.reactor.VehiclesService;
import org.example.reactor.persistence.ReservationsRepository;
import org.example.reactor.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RidesService {

    private static final Logger log = LoggerFactory.getLogger(RidesService.class);

    private final ReservationsRepository reservationsRepository;
    private final RidesEventsBus eventsBus;
    private final UsersRepository usersRepository;
    private final VehiclesService vehiclesService;

    public RidesService(ReservationsRepository reservationsRepository, RidesEventsBus eventsBus, UsersRepository usersRepository, VehiclesService vehiclesService) {
        this.reservationsRepository = reservationsRepository;
        this.eventsBus = eventsBus;
        this.usersRepository = usersRepository;
        this.vehiclesService = vehiclesService;
    }

    public Mono<Void> reservation(Reservation reservation) {
        return reservationsRepository.insert(reservation).flux()
                .flatMap(aVoid -> vehiclesService.getAvailableVehicles(reservation.getLocation()))
                .take(20)
                .map(vehicle -> new RideOffer(vehicle, reservation.getTime(), reservation.getLocation()))
                .flatMap(usersRepository::offerRide)
                .timeout(Duration.ofSeconds(30))
                .onErrorMap(e -> new RidesHandlingException("Failed to handle reservation " + reservation.getId(), e))
                .doOnComplete(() -> log.info("Handled reservation {}", reservation.getId()))
                .then();
    }

    public void handleBids() {
        eventsBus.subscribeToBids().doOnNext(bid -> {
            log.info("Bid: {}", bid);
        });
    }
}
