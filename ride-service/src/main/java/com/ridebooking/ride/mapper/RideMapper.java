package com.ridebooking.ride.mapper;

import com.ridebooking.ride.dto.response.RideResponse;
import com.ridebooking.ride.entity.Ride;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {

    public RideResponse mapToResponse(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getPassengerId(),
                ride.getDriverId(),
                ride.getVehicleType(),
                ride.getPaymentMethod(),
                ride.getPricingType(),
                ride.getStatus(),
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                ride.getDropLatitude(),
                ride.getDropLongitude(),
                ride.getEstimatedDistanceKm(),
                ride.getEstimatedFare(),
                ride.getFinalFare(),
                ride.getNotes(),
                ride.getCreatedAt(),
                ride.getUpdatedAt(),
                ride.getAssignedAt(),
                ride.getStartedAt(),
                ride.getCompletedAt(),
                ride.getCancelledAt()
        );
    }
}
