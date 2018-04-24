package org.example.reactor.persistence;

import org.example.reactor.Reservation;
import org.example.reactor.RideOffer;
import org.example.reactor.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UsersRepository extends ReactiveMongoRepository<RideOffer, String> {

    Mono<User> fetchUser(String name);

    Mono<Void> persistReservation(Reservation reservation);

    Mono<Void> offerRide(RideOffer offer);

}
