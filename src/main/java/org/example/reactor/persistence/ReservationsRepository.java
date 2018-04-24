package org.example.reactor.persistence;

import org.example.reactor.Reservation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReservationsRepository extends ReactiveMongoRepository<Reservation, String> {

    @Query("{ 'username': ?0 }")
    Flux<Reservation> fetchReservations(String userId);

}
