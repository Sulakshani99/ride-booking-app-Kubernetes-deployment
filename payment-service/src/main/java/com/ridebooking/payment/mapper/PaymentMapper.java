package com.ridebooking.payment.mapper;

import com.ridebooking.payment.dto.response.PaymentResponse;
import com.ridebooking.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getPassengerId(),
                payment.getDriverId(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getTransactionReference(),
                payment.getFailureReason(),
                payment.getProcessedAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
