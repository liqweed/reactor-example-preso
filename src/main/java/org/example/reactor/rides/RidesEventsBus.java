package org.example.reactor.rides;

import org.example.reactor.Reservation;
import org.example.reactor.RideBid;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

public interface RidesEventsBus {

    Flux<ReceiverRecord<String, Reservation>> subscribeToReservations();

    Flux<RideBid> subscribeToBids();
}
