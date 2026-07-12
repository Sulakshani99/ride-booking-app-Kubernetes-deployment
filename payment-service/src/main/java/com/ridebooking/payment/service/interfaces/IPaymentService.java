package com.ridebooking.payment.service.interfaces;

import com.ridebooking.payment.dto.request.UpdatePaymentStatusRequest;
import com.ridebooking.payment.dto.response.PaymentResponse;
import com.ridebooking.payment.dto.RideEvent;

import java.util.List;

public interface IPaymentService {

    PaymentResponse getPaymentById(Long paymentId);

    PaymentResponse getPaymentByRideId(Long rideId);

    List<PaymentResponse> getPassengerPayments(Long passengerId);

    PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request);

    PaymentResponse handleRideEvent(RideEvent event);
}
