package com.ridebooking.payment.exception;

public class PaymentServiceException extends RuntimeException {

    public PaymentServiceException(String message) {
        super(message);
    }
}
