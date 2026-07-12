package com.ridebooking.payment.controller;

import com.ridebooking.payment.dto.request.UpdatePaymentStatusRequest;
import com.ridebooking.payment.dto.response.PaymentResponse;
import com.ridebooking.payment.service.interfaces.IPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentByRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(paymentService.getPaymentByRideId(rideId));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<PaymentResponse>> getPassengerPayments(@PathVariable Long passengerId) {
        return ResponseEntity.ok(paymentService.getPassengerPayments(passengerId));
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updateStatus(
            @PathVariable Long paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) 
    {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, request));
    }
}
