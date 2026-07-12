package com.ridebooking.notification.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RideEvent(
        String eventType,
        Long rideId,
        Long passengerId,
        String passengerEmail,
        Long driverId,
        String driverEmail,
        String paymentMethod,
        String status,
        BigDecimal fare,
        Instant occurredAt
) {}