package com.ridebooking.ride.entity;

import com.ridebooking.ride.enums.PaymentMethod;
import com.ridebooking.ride.enums.PricingType;
import com.ridebooking.ride.enums.RideStatus;
import com.ridebooking.ride.enums.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long passengerId;

    @Column(nullable = false, length = 255)
    private String passengerEmail;

    @Column
    private Long driverId;

    @Column(length = 255)
    private String driverEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PricingType pricingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RideStatus status;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLatitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLongitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropLatitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropLongitude;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedDistanceKm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedFare;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalFare;

    @Column(length = 255)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private Instant assignedAt;

    @Column
    private Instant startedAt;

    @Column
    private Instant completedAt;

    @Column
    private Instant cancelledAt;

    // Simple constructor for ride creation
    public Ride(Long passengerId, String passengerEmail, VehicleType vehicleType, PaymentMethod paymentMethod,
                PricingType pricingType, BigDecimal pickupLatitude, BigDecimal pickupLongitude,
                BigDecimal dropLatitude, BigDecimal dropLongitude, BigDecimal estimatedDistanceKm,
                BigDecimal estimatedFare, String notes, RideStatus status) {
        this.passengerId = passengerId;
        this.passengerEmail = passengerEmail;
        this.vehicleType = vehicleType;
        this.paymentMethod = paymentMethod;
        this.pricingType = pricingType;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.dropLatitude = dropLatitude;
        this.dropLongitude = dropLongitude;
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.estimatedFare = estimatedFare;
        this.notes = notes;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
