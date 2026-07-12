package com.ridebooking.ride.dto.response;

import com.ridebooking.ride.enums.PricingType;
import com.ridebooking.ride.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateResponse {

        private VehicleType vehicleType;
        private PricingType pricingType;
        private BigDecimal distanceKm;
        private BigDecimal estimatedFare;
}
