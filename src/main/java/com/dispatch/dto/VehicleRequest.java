package com.dispatch.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequest(
        @NotBlank(message = "Vehicle ID is required")
        String vehicleId,

        @NotNull(message = "Capacity is required")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Capacity must be greater than zero")
        Double capacity,

        @NotNull(message = "Current latitude is required")
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double currentLatitude,

        @NotNull(message = "Current longitude is required")
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double currentLongitude,

        @NotBlank(message = "Current address is required")
        String currentAddress
) {
}