package com.ridebooking.ride.dto;

import com.ridebooking.ride.enums.PaymentMethod;
import com.ridebooking.ride.enums.RideStatus;

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
        RideStatus status,
        BigDecimal fare,
        Instant occurredAt
) {}