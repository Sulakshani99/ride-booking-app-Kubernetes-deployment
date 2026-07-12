package com.ridebooking.payment.repository;

import com.ridebooking.payment.entity.Payment;
import com.ridebooking.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRideId(Long rideId);

    List<Payment> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
}
