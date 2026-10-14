package com.dispatch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "delivery_orders")
public class DeliveryOrder {

    @Id
    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Package weight is required")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Package weight must be greater than zero")
    private Double packageWeight;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    protected DeliveryOrder() {
        // Required by JPA
    }

    public DeliveryOrder(
            String orderId,
            Double latitude,
            Double longitude,
            String address,
            Double packageWeight,
            Priority priority) {
        this.orderId = orderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.packageWeight = packageWeight;
        this.priority = priority;
    }

    public String getOrderId() {
        return orderId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public Double getPackageWeight() {
        return packageWeight;
    }

    public Priority getPriority() {
        return priority;
    }
}