package com.ridebooking.ride.controller;

import com.ridebooking.ride.dto.request.AssignDriverRequest;
import com.ridebooking.ride.dto.request.BookRideRequest;
import com.ridebooking.ride.dto.request.FareEstimateRequest;
import com.ridebooking.ride.dto.request.UpdateRideStatusRequest;
import com.ridebooking.ride.dto.response.FareEstimateResponse;
import com.ridebooking.ride.dto.response.RideResponse;
import com.ridebooking.ride.exception.RideServiceException;
import com.ridebooking.ride.security.JwtClaimsExtractor;
import com.ridebooking.ride.service.interfaces.IRideService;
import com.ridebooking.ride.enums.RideStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {

    private final IRideService rideService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public RideController(IRideService rideService, JwtClaimsExtractor jwtClaimsExtractor) {
        this.rideService = rideService;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        return ResponseEntity.ok(rideService.estimateFare(request));
    }

    @PostMapping
    public ResponseEntity<RideResponse> bookRide(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody BookRideRequest request
    ) {
        Long passengerId = jwtClaimsExtractor.extractUserId(authorizationHeader);
        String passengerEmail = jwtClaimsExtractor.extractEmail(authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.bookRide(passengerId, passengerEmail, request));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.getRideById(rideId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<RideResponse>> getHistory(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long userId = jwtClaimsExtractor.extractUserId(authorizationHeader);
        String role = jwtClaimsExtractor.extractRole(authorizationHeader);
        return ResponseEntity.ok(resolveHistoryForRole(role, userId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<RideResponse>> getAvailableRides(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String role = jwtClaimsExtractor.extractRole(authorizationHeader);
        if (!"DRIVER".equalsIgnoreCase(role)) {
            throw new RideServiceException("Available ride requests are visible only to drivers");
        }

        return ResponseEntity.ok(rideService.getAvailableRideRequests());
    }

    private List<RideResponse> resolveHistoryForRole(String role, Long userId) {
        return switch (role.toUpperCase()) {
            case "DRIVER" -> rideService.getDriverRideHistory(userId);
            case "USER" -> rideService.getPassengerRideHistory(userId);
            default -> throw new RideServiceException(
                    "Ride history is available only for passenger and driver accounts, not role: " + role
            );
        };
    }

    @PatchMapping("/{rideId}/assign-driver")
    public ResponseEntity<RideResponse> assignDriver(
            @PathVariable Long rideId,
            @Valid @RequestBody AssignDriverRequest request
    ) {
        return ResponseEntity.ok(rideService.assignDriver(rideId, request));
    }

    @PatchMapping("/{rideId}/status")
    public ResponseEntity<RideResponse> updateStatus(
            @PathVariable Long rideId,
            @Valid @RequestBody UpdateRideStatusRequest request
    ) {
        return ResponseEntity.ok(rideService.updateRideStatus(rideId, request));
    }
}
