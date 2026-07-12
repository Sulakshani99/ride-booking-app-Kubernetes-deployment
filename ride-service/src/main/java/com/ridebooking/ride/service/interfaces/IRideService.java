package com.ridebooking.ride.service.interfaces;

import com.ridebooking.ride.dto.request.AssignDriverRequest;
import com.ridebooking.ride.dto.request.BookRideRequest;
import com.ridebooking.ride.dto.request.FareEstimateRequest;
import com.ridebooking.ride.dto.request.UpdateRideStatusRequest;
import com.ridebooking.ride.dto.response.FareEstimateResponse;
import com.ridebooking.ride.dto.response.RideResponse;

import java.util.List;

public interface IRideService {

    RideResponse bookRide(Long passengerId, String passengerEmail, BookRideRequest request);

    FareEstimateResponse estimateFare(FareEstimateRequest request);

    RideResponse getRideById(Long rideId);

    List<RideResponse> getPassengerRideHistory(Long passengerId);

    List<RideResponse> getDriverRideHistory(Long driverId);

    List<RideResponse> getAvailableRideRequests();

    RideResponse assignDriver(Long rideId, AssignDriverRequest request);

    RideResponse updateRideStatus(Long rideId, UpdateRideStatusRequest request);
}
