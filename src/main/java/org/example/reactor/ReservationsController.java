package org.example.reactor;

import org.example.reactor.persistence.ReservationsRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@RestController
public class ReservationsController {

    private final ReservationsRepository reservationsRepository;
    private final Clock clock;

    public ReservationsController(ReservationsRepository reservationsRepository, Clock clock) {
        this.reservationsRepository = reservationsRepository;
        this.clock = clock;
    }

    @GetMapping("/{userId}")
    public Flux<ReservationDTO> getReservations(@PathVariable("userId") String userId) {
        return reservationsRepository.fetchReservations(userId)
                .filter(reservation -> isRecent(reservation, 1, ChronoUnit.HOURS))
                .map(this::toReservationDTO);
    }

    private boolean isRecent(Reservation reservation, long amountToSubtract, TemporalUnit unit) {
        return reservation.getTime().isAfter(clock.instant().minus(amountToSubtract, unit));
    }

    @NotNull
    private ReservationDTO toReservationDTO(Reservation reservation) {
        return new ReservationDTO(reservation.getId(), reservation.getTime(), reservation.getUserId(), reservation.getLocation());
    }
}
