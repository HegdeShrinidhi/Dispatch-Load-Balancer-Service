package com.dispatch.dto;

import com.dispatch.entity.Priority;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude,

        @NotBlank(message = "Address is required")
        String address,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        Double packageWeight,

        @NotNull
        Priority priority
) {
}