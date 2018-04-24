package org.example.reactor.blocking;

import org.example.reactor.Reservation;
import org.example.reactor.ReservationDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@RequestMapping("/reservationsBlock")
@RestController
public class BlockingReservationsController {

    private final BlockingReservationsRepository repository;
    private final Clock clock;

    public BlockingReservationsController(BlockingReservationsRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @RequestMapping(path = "/{userId}", method = RequestMethod.GET)
    public List<ReservationDTO> getReservations1(@PathVariable("userId") String userId) {
        List<ReservationDTO> list = new ArrayList<>();
        for (Reservation reservation : repository.fetchReservations(userId)) {
            ReservationDTO reservationDTO = toReservationDTO(reservation);
            list.add(reservationDTO);
        }
        return list;
    }

    @RequestMapping(path = "/{userId}", method = RequestMethod.GET)
    public List<ReservationDTO> getReservations2(@PathVariable("userId") String userId) {
        List<Reservation> fetchReservations = repository.fetchReservations(userId);
        return fetchReservations.stream()
                .filter(reservation -> isRecent(reservation, 1, HOURS))
                .map(this::toReservationDTO)
                .collect(Collectors.toList());
    }

    private boolean isRecent(Reservation reservation, long amountToSubtract, TemporalUnit unit) {
        return reservation.getTime().isAfter(clock.instant().minus(amountToSubtract, unit));
    }

    @NotNull
    private ReservationDTO toReservationDTO(Reservation reservation) {
        return new ReservationDTO(reservation.getId(), reservation.getTime(), reservation.getUserId(), reservation.getLocation());
    }
}
