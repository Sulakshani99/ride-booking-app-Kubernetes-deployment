package com.ridebooking.ride.exception;

public class InvalidRideStatusTransitionException extends RuntimeException {

    public InvalidRideStatusTransitionException(String message) {
        super(message);
    }
}
