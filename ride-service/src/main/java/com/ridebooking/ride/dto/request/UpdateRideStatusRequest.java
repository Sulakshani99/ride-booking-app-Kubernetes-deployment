package com.ridebooking.ride.dto.request;

import com.ridebooking.ride.enums.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRideStatusRequest {

        @NotNull
        private RideStatus status;
}
