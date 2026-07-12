package com.ridebooking.payment.service.impl;

import com.ridebooking.payment.dto.request.UpdatePaymentStatusRequest;
import com.ridebooking.payment.dto.response.PaymentResponse;
import com.ridebooking.payment.dto.RideEvent;
import com.ridebooking.payment.entity.Payment;
import com.ridebooking.payment.enums.PaymentMethod;
import com.ridebooking.payment.enums.PaymentStatus;
import com.ridebooking.payment.exception.InvalidPaymentStatusTransitionException;
import com.ridebooking.payment.exception.PaymentNotFoundException;
import com.ridebooking.payment.exception.PaymentServiceException;
import com.ridebooking.payment.mapper.PaymentMapper;
import com.ridebooking.payment.repository.PaymentRepository;
import com.ridebooking.payment.service.interfaces.IPaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        return paymentMapper.mapToResponse(getPaymentEntity(paymentId));
    }

    @Override
    public PaymentResponse getPaymentByRideId(Long rideId) {
        return paymentMapper.mapToResponse(getPaymentEntityByRideId(rideId));
    }

    @Override
    public List<PaymentResponse> getPassengerPayments(Long passengerId) {
        return paymentRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId).stream()
                .map(paymentMapper::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request) 
    {
        Payment payment = getPaymentEntity(paymentId);

        validateStatusTransition(payment.getStatus(), request.status());

        payment.setStatus(request.status());
        if (request.status() == PaymentStatus.FAILED) {
            payment.setFailureReason(request.failureReason());
        } else {
            payment.setFailureReason(null);
        }
        payment.setProcessedAt(Instant.now());

        return paymentMapper.mapToResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse handleRideEvent(RideEvent event) {
        if (event == null) {
            throw new PaymentServiceException("Ride event must not be null");
        }

        if (!"RIDE_COMPLETED".equalsIgnoreCase(event.eventType())
                && !"RIDE_CANCELLED".equalsIgnoreCase(event.eventType())) {
            return null;
        }

        Payment payment = paymentRepository.findByRideId(event.rideId()).orElseGet(Payment::new);
        payment.setRideId(event.rideId());
        payment.setPassengerId(event.passengerId());
        payment.setDriverId(event.driverId());
        payment.setPaymentMethod(resolvePaymentMethod(event.paymentMethod()));
        payment.setAmount(resolveAmount(event.fare()));

        if ("RIDE_CANCELLED".equalsIgnoreCase(event.eventType())) {
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setProcessedAt(Instant.now());
            payment.setFailureReason("Ride cancelled");
        } else if (payment.getPaymentMethod() == PaymentMethod.CASH) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setProcessedAt(Instant.now());
            payment.setFailureReason(null);
        } else if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
            payment.setProcessedAt(null);
            payment.setFailureReason(null);
        }

        if (payment.getTransactionReference() == null) {
            payment.setTransactionReference(buildTransactionReference(event.rideId()));
        }

        return paymentMapper.mapToResponse(paymentRepository.save(payment));
    }

    private Payment getPaymentEntity(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
    }

    private Payment getPaymentEntityByRideId(Long rideId) {
        return paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ride id: " + rideId));
    }

    private void validateStatusTransition(PaymentStatus current, PaymentStatus target) {
        boolean valid = switch (current) {
            case PENDING -> target == PaymentStatus.COMPLETED || target == PaymentStatus.FAILED || target == PaymentStatus.CANCELLED;
            case FAILED -> target == PaymentStatus.PENDING;
            case COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new InvalidPaymentStatusTransitionException(
                    "Invalid payment status transition from " + current + " to " + target
            );
        }
    }

    private PaymentMethod resolvePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new PaymentServiceException("Payment method is required in ride event");
        }
        return paymentMethod;
    }

    private BigDecimal resolveAmount(BigDecimal amount) {
        if (amount == null) {
            throw new PaymentServiceException("Ride fare is required in ride event");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String buildTransactionReference(Long rideId) {
        return "PAY-" + rideId + "-" + Instant.now().toEpochMilli();
    }
}
