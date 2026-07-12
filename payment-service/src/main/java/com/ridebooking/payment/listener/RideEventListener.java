package com.ridebooking.payment.listener;

import com.ridebooking.payment.dto.RideEvent;
import com.ridebooking.payment.service.interfaces.IPaymentService;
import org.springframework.stereotype.Component;

@Component
public class RideEventListener {

    private final IPaymentService paymentService;

    public RideEventListener(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Kafka removed: listener is retained as a no-op receiver for future integration
    public void onRideEvent(RideEvent event) {
        paymentService.handleRideEvent(event);
    }
}
