package com.ridebooking.payment.dto.response;

import com.ridebooking.payment.enums.PaymentMethod;
import com.ridebooking.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long rideId,
        Long passengerId,
        Long driverId,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        BigDecimal amount,
        String transactionReference,
        String failureReason,
        Instant processedAt,
        Instant createdAt,
        Instant updatedAt
) {}
