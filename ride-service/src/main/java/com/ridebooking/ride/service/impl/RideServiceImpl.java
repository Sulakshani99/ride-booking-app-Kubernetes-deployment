package com.ridebooking.ride.service.impl;

import com.ridebooking.ride.dto.request.AssignDriverRequest;
import com.ridebooking.ride.dto.request.BookRideRequest;
import com.ridebooking.ride.dto.request.FareEstimateRequest;
import com.ridebooking.ride.dto.request.UpdateRideStatusRequest;
import com.ridebooking.ride.dto.response.FareEstimateResponse;
import com.ridebooking.ride.dto.response.RideResponse;
import com.ridebooking.ride.entity.Ride;
import com.ridebooking.ride.enums.PricingType;
import com.ridebooking.ride.enums.RideStatus;
import com.ridebooking.ride.enums.VehicleType;
import com.ridebooking.ride.exception.InvalidRideStatusTransitionException;
import com.ridebooking.ride.exception.RideNotFoundException;
import com.ridebooking.ride.exception.RideServiceException;
import com.ridebooking.ride.mapper.RideMapper;
import com.ridebooking.ride.patterns.observer.RideEventPublisher;
import com.ridebooking.ride.repository.RideRepository;
import com.ridebooking.ride.service.interfaces.IRideService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class RideServiceImpl implements IRideService {

    private static final Set<RideStatus> ACTIVE_STATUSES = Set.of(
            RideStatus.REQUESTED,
            RideStatus.DRIVER_ASSIGNED,
            RideStatus.DRIVER_ARRIVING,
            RideStatus.TRIP_STARTED
    );

    private static final BigDecimal BASE_FARE = new BigDecimal("150.00");

    private final RideRepository rideRepository;
    private final RideMapper rideMapper;
    private final RideEventPublisher rideEventPublisher;

    public RideServiceImpl(
            RideRepository rideRepository,
            RideMapper rideMapper,
            RideEventPublisher rideEventPublisher
    ) {
        this.rideRepository = rideRepository;
        this.rideMapper = rideMapper;
        this.rideEventPublisher = rideEventPublisher;
    }

    @Override
    @Transactional
    public RideResponse bookRide(Long passengerId, String passengerEmail, BookRideRequest request) 
    {
        ensurePassengerHasNoActiveRide(passengerId);

        BigDecimal distanceKm = calculateDistanceKm(
            request.getPickupLatitude(),
            request.getPickupLongitude(),
            request.getDropLatitude(),
            request.getDropLongitude()
        );

        BigDecimal estimatedFare = calculateEstimatedFare(distanceKm, request.getVehicleType());

        Ride ride = new Ride(
                passengerId,
                passengerEmail,
                request.getVehicleType(),
                request.getPaymentMethod(),
                PricingType.NORMAL,
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropLatitude(),
                request.getDropLongitude(),
                distanceKm,
                estimatedFare,
                request.getNotes(),
                RideStatus.REQUESTED
        );

        Ride savedRide = rideRepository.save(ride);
        rideEventPublisher.publish("RIDE_REQUESTED", savedRide);
        return rideMapper.mapToResponse(savedRide);
    }

    @Override
    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        BigDecimal distanceKm = calculateDistanceKm(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropLatitude(),
                request.getDropLongitude()
        );
        BigDecimal estimatedFare = calculateEstimatedFare(distanceKm, request.getVehicleType());
        return new FareEstimateResponse(request.getVehicleType(), PricingType.NORMAL, distanceKm, estimatedFare);
    }

    @Override
    public RideResponse getRideById(Long rideId) {
        return rideMapper.mapToResponse(getRideEntity(rideId));
    }

    @Override
    public List<RideResponse> getPassengerRideHistory(Long passengerId) {
        List<Ride> passengerRides = rideRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
        return passengerRides.stream()
                .map(rideMapper::mapToResponse)
                .toList();
    }

    @Override
    public List<RideResponse> getDriverRideHistory(Long driverId) {
        List<Ride> driverRides = rideRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        return driverRides.stream()
                .map(rideMapper::mapToResponse)
                .toList();
    }

    @Override
    public List<RideResponse> getAvailableRideRequests() {
        List<Ride> requestedRides = rideRepository.findByStatusOrderByCreatedAtDesc(RideStatus.REQUESTED);
        return requestedRides.stream()
                .map(rideMapper::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public RideResponse assignDriver(Long rideId, AssignDriverRequest request) 
    {
        Ride ride = getRideEntity(rideId);
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideStatusTransitionException("Driver can only be assigned to a requested ride");
        }

        ensureDriverHasNoActiveRide(request.getDriverId());

        ride.setDriverId(request.getDriverId());
        ride.setDriverEmail(request.getDriverEmail());
        ride.setAssignedAt(Instant.now());
        ride.setStatus(RideStatus.DRIVER_ASSIGNED);
        Ride savedRide = rideRepository.save(ride);
        rideEventPublisher.publish("DRIVER_ASSIGNED", savedRide);
        return rideMapper.mapToResponse(savedRide);
    }

    @Override
    @Transactional
    public RideResponse updateRideStatus(Long rideId, UpdateRideStatusRequest request) 
    {
        Ride ride = getRideEntity(rideId);

        validateStatusTransition(ride.getStatus(), request.getStatus(), ride.getDriverId());

        ride.setStatus(request.getStatus());
        
        switch (request.getStatus()) {
            case DRIVER_ASSIGNED -> {
                if (ride.getDriverId() == null) {
                    throw new RideServiceException("Driver must be assigned before setting status to DRIVER_ASSIGNED");
                }
                ride.setAssignedAt(Instant.now());
            }
            case DRIVER_ARRIVING -> {
                if (ride.getDriverId() == null) {
                    throw new RideServiceException("Driver must be assigned before arriving");
                }
            }
            case TRIP_STARTED -> {
                ride.setStartedAt(Instant.now());
            }
            case COMPLETED -> {
                ride.setCompletedAt(Instant.now());
                ride.setFinalFare(ride.getEstimatedFare());
            }
            case CANCELLED -> {
                ride.setCancelledAt(Instant.now());
            }
            case REQUESTED -> throw new InvalidRideStatusTransitionException("Ride cannot transition back to REQUESTED");
        }
        Ride savedRide = rideRepository.save(ride);

        // publish events after successful save to ensure listeners see persisted state
        switch (savedRide.getStatus()) {
            case DRIVER_ASSIGNED -> rideEventPublisher.publish("DRIVER_ASSIGNED", savedRide);
            case TRIP_STARTED -> rideEventPublisher.publish("TRIP_STARTED", savedRide);
            case COMPLETED -> rideEventPublisher.publish("RIDE_COMPLETED", savedRide);
            case CANCELLED -> rideEventPublisher.publish("RIDE_CANCELLED", savedRide);
            default -> { }
        }

        return rideMapper.mapToResponse(savedRide);
    }

    private Ride getRideEntity(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + rideId));
    }

    private void ensurePassengerHasNoActiveRide(Long passengerId) {
        rideRepository.findFirstByPassengerIdAndStatusInOrderByCreatedAtDesc(passengerId, List.copyOf(ACTIVE_STATUSES))
                .ifPresent(ride -> {
                    throw new RideServiceException("Passenger already has an active ride");
                });
    }

    private void ensureDriverHasNoActiveRide(Long driverId) {
        rideRepository.findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(driverId, List.copyOf(ACTIVE_STATUSES))
                .ifPresent(ride -> {
                    throw new RideServiceException("Driver already has an active ride");
                });
    }

    private void validateStatusTransition(RideStatus current, RideStatus target, Long driverId) 
    {
        boolean valid = switch (current) {
            case REQUESTED -> target == RideStatus.DRIVER_ASSIGNED || target == RideStatus.CANCELLED;
            case DRIVER_ASSIGNED -> target == RideStatus.DRIVER_ARRIVING
                    || target == RideStatus.TRIP_STARTED
                    || target == RideStatus.CANCELLED;
            case DRIVER_ARRIVING -> target == RideStatus.TRIP_STARTED || target == RideStatus.CANCELLED;
            case TRIP_STARTED -> target == RideStatus.COMPLETED || target == RideStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new InvalidRideStatusTransitionException(
                    "Invalid ride status transition from " + current + " to " + target
            );
        }

        if (target != RideStatus.CANCELLED && target != RideStatus.REQUESTED && driverId == null) {
            throw new RideServiceException("Driver must be assigned before this status transition");
        }
    }

    private BigDecimal calculateDistanceKm(
            BigDecimal pickupLatitude,
            BigDecimal pickupLongitude,
            BigDecimal dropLatitude,
            BigDecimal dropLongitude) 
    {
        double earthRadiusKm = 6371.0;
        double latDistance = Math.toRadians(dropLatitude.doubleValue() - pickupLatitude.doubleValue());
        double lonDistance = Math.toRadians(dropLongitude.doubleValue() - pickupLongitude.doubleValue());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(pickupLatitude.doubleValue()))
                * Math.cos(Math.toRadians(dropLatitude.doubleValue()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadiusKm * c;

        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEstimatedFare(BigDecimal distanceKm, VehicleType vehicleType) {
        BigDecimal perKmRate = switch (vehicleType) {
            case TUK -> new BigDecimal("80.00");
            case CAR -> new BigDecimal("120.00");
            case VAN -> new BigDecimal("160.00");
        };
        return BASE_FARE.add(distanceKm.multiply(perKmRate)).setScale(2, RoundingMode.HALF_UP);
    }
}
