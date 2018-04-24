package org.example.reactor.blocking;

import org.example.reactor.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface BlockingReservationsRepository extends MongoRepository<Reservation, String> {

    @Query("{ 'username': ?0 }")
    List<Reservation> fetchReservations(String userId);
}
