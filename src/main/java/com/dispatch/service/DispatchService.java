package com.dispatch.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dispatch.dto.DispatchPlanResponse;
import com.dispatch.entity.DeliveryOrder;
import com.dispatch.entity.Vehicle;
import com.dispatch.optimizer.DispatchOptimizer;
import com.dispatch.repository.OrderRepository;
import com.dispatch.repository.VehicleRepository;

@Service
public class DispatchService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchOptimizer dispatchOptimizer;

    public DispatchService(
            OrderRepository orderRepository,
            VehicleRepository vehicleRepository,
            DispatchOptimizer dispatchOptimizer) {
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
        this.dispatchOptimizer = dispatchOptimizer;
    }

    @Transactional(readOnly = true)
    public DispatchPlanResponse generateDispatchPlan() {
        List<DeliveryOrder> orders = orderRepository.findAll();
        List<Vehicle> vehicles = vehicleRepository.findAll();

        return dispatchOptimizer.optimize(orders, vehicles);
    }
}
