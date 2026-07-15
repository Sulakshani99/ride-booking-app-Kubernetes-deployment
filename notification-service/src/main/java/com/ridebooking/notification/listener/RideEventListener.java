package com.ridebooking.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridebooking.notification.dto.RideEvent;
import com.ridebooking.notification.service.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.math.BigDecimal;
import java.util.List;

@Component
public class RideEventListener {

    private static final Logger log = LoggerFactory.getLogger(RideEventListener.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final String queueUrl;

    public RideEventListener(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            EmailService emailService,
            @Value("${aws.sqs.notification-queue-url:}") String queueUrl
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.emailService = emailService;
        this.queueUrl = queueUrl;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollQueue() {
        if (queueUrl == null || queueUrl.isBlank()) {
            return;
        }

        List<Message> messages = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(10)
                        .build())
                .messages();

        for (Message message : messages) {
            try {
                RideEvent event = objectMapper.readValue(message.body(), RideEvent.class);
                onRideEvent(event);
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            } catch (Exception e) {
                log.error("Failed to process ride event; leaving on queue for retry", e);
            }
        }
    }

    private void onRideEvent(RideEvent event) {
        log.info("Received ride event: {} for rideId={}", event.eventType(), event.rideId());

        String subject = "Ride update: " + event.eventType().replace('_', ' ');
        String bodyText = buildBodyText(event);
        String bodyHtml = "<div>" + bodyText.replace("\n", "<br/>") + "</div>";

        try {
            if (event.passengerEmail() != null) {
                emailService.sendEmail(event.passengerEmail(), subject, bodyHtml, bodyText);
            }
            if (event.driverEmail() != null) {
                emailService.sendEmail(event.driverEmail(), subject, bodyHtml, bodyText);
            }
        } catch (Exception e) {
            log.error("Failed to send notification emails", e);
            throw new RuntimeException(e); // rethrow so the message isn't deleted and gets retried
        }
    }

    private String buildBodyText(RideEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello,").append("\n\n");
        sb.append("This is an automated notification from Ride Booking App regarding your ride.").append("\n\n");
        sb.append("Event: ").append(event.eventType()).append("\n");
        sb.append("Ride ID: ").append(event.rideId()).append("\n");
        sb.append("Status: ").append(event.status()).append("\n");
        BigDecimal fare = event.fare();
        if (fare != null) sb.append("Fare: Rs. ").append(fare).append("\n");
        sb.append("Occurred At: ").append(event.occurredAt()).append("\n\n");
        sb.append("If you have any questions, please Call.").append("\n\n");
        sb.append("Best regards,").append("\n");
        sb.append("Ride Booking Team");
        return sb.toString();
    }
}