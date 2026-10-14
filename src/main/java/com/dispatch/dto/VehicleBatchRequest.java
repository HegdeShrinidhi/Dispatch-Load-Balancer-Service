package com.dispatch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record VehicleBatchRequest(
        @NotEmpty(message = "At least one vehicle is required")
        List<@Valid VehicleRequest> vehicles
) {
}