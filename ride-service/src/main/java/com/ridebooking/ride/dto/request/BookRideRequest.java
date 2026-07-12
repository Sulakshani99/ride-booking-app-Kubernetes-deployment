package com.ridebooking.ride.dto.request;

import com.ridebooking.ride.enums.PaymentMethod;
import com.ridebooking.ride.enums.VehicleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRideRequest {

        @NotNull
        private VehicleType vehicleType;

        @NotNull
        private PaymentMethod paymentMethod;

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        private BigDecimal pickupLatitude;

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        private BigDecimal pickupLongitude;

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        private BigDecimal dropLatitude;

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        private BigDecimal dropLongitude;

        @Size(max = 255)
        private String notes;
}
