package com.ridebooking.ride.patterns.observer;

import com.ridebooking.ride.entity.Ride;
import com.ridebooking.ride.dto.RideEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RideEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RideEventPublisher.class);

    public RideEventPublisher() {
    }

    public void publish(String eventType, Ride ride) {
        RideEvent event = new RideEvent(
                eventType,
                ride.getId(),
                ride.getPassengerId(),
                ride.getPassengerEmail(),
                ride.getDriverId(),
                ride.getDriverEmail(),
                ride.getPaymentMethod(),
                ride.getStatus(),
                ride.getFinalFare() != null ? ride.getFinalFare() : ride.getEstimatedFare(),
                Instant.now()
        );
        // Kafka removed for local deployments; this is a no-op publisher that logs the event
        log.info("RideEvent (kafka-disabled): {} for rideId={}", event.eventType(), event.rideId());
    }
}
