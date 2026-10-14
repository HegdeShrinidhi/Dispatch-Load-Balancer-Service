package com.dispatch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    @NotNull(message = "Vehicle capacity is required")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Capacity must be greater than zero")
    private Double capacity;

    @NotNull(message = "Current latitude is required")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double currentLatitude;

    @NotNull(message = "Current longitude is required")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double currentLongitude;

    @NotBlank(message = "Current address is required")
    private String currentAddress;

    protected Vehicle() {
        // Required by JPA
    }

    public Vehicle(
            String vehicleId,
            Double capacity,
            Double currentLatitude,
            Double currentLongitude,
            String currentAddress) {
        this.vehicleId = vehicleId;
        this.capacity = capacity;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.currentAddress = currentAddress;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public Double getCapacity() {
        return capacity;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }
}