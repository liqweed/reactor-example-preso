package org.example.reactor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.reactor.rides.RidesEventsBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaEventsBus implements RidesEventsBus {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventsBus.class);

    private final String bootstrapServer;
    private final DateTimeFormatter dateTimeFormatter;
    private final String reservationsTopic = "reservations";
    private Disposable subscription;

    public KafkaEventsBus(@Value("${kafka.server.bootstrap:localhost:9092}") String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
        dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS z dd MMM yyyy");
    }

    @Override
    public Flux<ReceiverRecord<String, Reservation>> subscribeToReservations() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ReceiverOptions<String, Reservation> receiverOptions = ReceiverOptions.<String, Reservation>create(props)
                .subscription(Collections.singleton(reservationsTopic))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        Flux<ReceiverRecord<String, Reservation>> kafkaFlux = KafkaReceiver.create(receiverOptions).receive();
        subscription = kafkaFlux.subscribe(record -> {
            ReceiverOffset offset = record.receiverOffset();
            log.info("Received message: topic-partition={} offset={} timestamp={} key={} value={}",
                    offset.topicPartition(),
                    offset.offset(),
                    dateTimeFormatter.format(Instant.ofEpochMilli(record.timestamp())),
                    record.key(),
                    record.value());
            offset.acknowledge();
        });
        return kafkaFlux;
    }

    @Override
    public Flux<RideBid> subscribeToBids() {
        return null;
    }

    @PreDestroy
    public void shutdown() {
        subscription.dispose();
    }
}
