package com.ridebooking.notification.listener;

import com.ridebooking.notification.dto.RideEvent;
import com.ridebooking.notification.service.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RideEventListener {

    private static final Logger log = LoggerFactory.getLogger(RideEventListener.class);

    private final EmailService emailService;

    public RideEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    // Kafka removed: retained as a no-op receiver to reuse existing handling logic
    public void onRideEvent(RideEvent event) {
        log.info("(kafka-disabled) Received ride event: {} for rideId={}", event.eventType(), event.rideId());

        String passengerEmail = event.passengerEmail();
        String driverEmail = event.driverEmail();

        String subject = "Ride update: " + event.eventType().replace('_', ' ');
        String bodyText = buildBodyText(event);
        String bodyHtml = "<div>" + bodyText.replace("\n", "<br/>") + "</div>";

        try {
            if (passengerEmail != null) {
                emailService.sendEmail(passengerEmail, subject, bodyHtml, bodyText);
            }
            if (driverEmail != null) {
                emailService.sendEmail(driverEmail, subject, bodyHtml, bodyText);
            }
        } catch (Exception e) {
            log.error("Failed to send notification emails", e);
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
