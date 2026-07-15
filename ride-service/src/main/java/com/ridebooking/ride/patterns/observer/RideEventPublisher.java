package com.ridebooking.ride.patterns.observer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridebooking.ride.dto.RideEvent;
import com.ridebooking.ride.entity.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.util.List;

@Component
public class RideEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RideEventPublisher.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final List<String> queueUrls;

    public RideEventPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.payment-queue-url:}") String paymentQueueUrl,
            @Value("${aws.sqs.notification-queue-url:}") String notificationQueueUrl
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrls = List.of(paymentQueueUrl, notificationQueueUrl).stream()
                .filter(url -> url != null && !url.isBlank())
                .toList();
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

        if (queueUrls.isEmpty()) {
            log.warn("No SQS queue URLs configured; dropping RideEvent {} for rideId={}", eventType, ride.getId());
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("Failed to serialize RideEvent for rideId={}", ride.getId(), e);
            return;
        }

        for (String queueUrl : queueUrls) {
            try {
                sqsClient.sendMessage(SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(payload)
                        .build());
            } catch (Exception e) {
                log.error("Failed to publish RideEvent {} to queue {}", eventType, queueUrl, e);
            }
        }

        log.info("Published RideEvent {} for rideId={} to {} queue(s)", eventType, ride.getId(), queueUrls.size());
    }
}