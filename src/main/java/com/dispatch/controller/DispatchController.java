package com.dispatch.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dispatch.dto.ApiResponse;
import com.dispatch.dto.DispatchPlanResponse;
import com.dispatch.dto.OrderBatchRequest;
import com.dispatch.dto.VehicleBatchRequest;
import com.dispatch.service.DispatchService;
import com.dispatch.service.OrderService;
import com.dispatch.service.VehicleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final DispatchService dispatchService;

    public DispatchController(
            OrderService orderService,
            VehicleService vehicleService,
            DispatchService dispatchService) {
        this.orderService = orderService;
        this.vehicleService = vehicleService;
        this.dispatchService = dispatchService;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse addOrders(
            @Valid @RequestBody OrderBatchRequest request) {

        orderService.saveOrders(request.orders());

        return new ApiResponse(
                "Delivery orders accepted.",
                "success"
        );
    }

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse addVehicles(
            @Valid @RequestBody VehicleBatchRequest request) {

        vehicleService.saveVehicles(request.vehicles());

        return new ApiResponse(
                "Vehicle details accepted.",
                "success"
        );
    }

    @GetMapping("/plan")
    public DispatchPlanResponse getDispatchPlan() {
        return dispatchService.generateDispatchPlan();
    }
}
