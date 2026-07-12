package com.ridebooking.ride.repository;

import com.ridebooking.ride.entity.Ride;
import com.ridebooking.ride.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    List<Ride> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Ride> findByStatusOrderByCreatedAtDesc(RideStatus status);

    Optional<Ride> findFirstByPassengerIdAndStatusInOrderByCreatedAtDesc(Long passengerId, List<RideStatus> statuses);

    Optional<Ride> findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(Long driverId, List<RideStatus> statuses);
}
