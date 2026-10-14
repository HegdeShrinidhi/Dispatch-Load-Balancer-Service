package com.dispatch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderBatchRequest(
        @NotEmpty(message = "At least one order is required")
        List<@Valid OrderRequest> orders
) {
}