package com.ridebooking.ride.dto.response;

import com.ridebooking.ride.enums.PaymentMethod;
import com.ridebooking.ride.enums.PricingType;
import com.ridebooking.ride.enums.RideStatus;
import com.ridebooking.ride.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {

        private Long id;
        private Long passengerId;
        private Long driverId;
        private VehicleType vehicleType;
        private PaymentMethod paymentMethod;
        private PricingType pricingType;
        private RideStatus status;
        private BigDecimal pickupLatitude;
        private BigDecimal pickupLongitude;
        private BigDecimal dropLatitude;
        private BigDecimal dropLongitude;
        private BigDecimal estimatedDistanceKm;
        private BigDecimal estimatedFare;
        private BigDecimal finalFare;
        private String notes;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant assignedAt;
        private Instant startedAt;
        private Instant completedAt;
        private Instant cancelledAt;
}
