package com.ridebooking.payment.dto;

import com.ridebooking.payment.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record RideEvent(
        String eventType,
        Long rideId,
        Long passengerId,
        String passengerEmail,
        Long driverId,
        String driverEmail,
        PaymentMethod paymentMethod,
        String status,
        BigDecimal fare,
        Instant occurredAt
) {}