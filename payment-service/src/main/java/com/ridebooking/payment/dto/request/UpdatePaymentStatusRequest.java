package com.ridebooking.payment.dto.request;

import com.ridebooking.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePaymentStatusRequest(
        @NotNull PaymentStatus status,
        @Size(max = 255) String failureReason
) {}
