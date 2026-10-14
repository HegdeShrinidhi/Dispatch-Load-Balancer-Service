package com.dispatch.optimizer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.dispatch.dto.DispatchPlanResponse;
import com.dispatch.entity.DeliveryOrder;
import com.dispatch.entity.Priority;
import com.dispatch.entity.Vehicle;
import com.dispatch.util.HaversineDistanceCalculator;

/**
 * Greedy optimization implementation for balancing delivery loads across a
 * vehicle fleet. Prioritizes high-priority items and minimizes routing distance
 * within capacity constraints.
 */
@Component
public class GreedyDispatchOptimizer implements DispatchOptimizer {

    @Override
    public DispatchPlanResponse optimize(List<DeliveryOrder> orders, List<Vehicle> vehicles) {
        if (orders == null || vehicles == null) {
            throw new IllegalArgumentException("Orders and vehicles must not be null");
        }

        // Work on a mutable copy to keep original parameter collections safe
        List<DeliveryOrder> sortedOrders = new ArrayList<>(orders);

        // 1. Sort by Priority tiering strictly: HIGH -> MEDIUM -> LOW
        // Uses unique Order ID as a predictable tie-breaker for deterministic evaluation results
        sortedOrders.sort((first, second) -> {
            int priorityComparison = Integer.compare(
                    priorityRank(first.getPriority()),
                    priorityRank(second.getPriority()));

            if (priorityComparison != 0) {
                return priorityComparison;
            }
            return first.getOrderId().compareTo(second.getOrderId());
        });

        // Initialize state contexts for processing live routing capacity steps
        List<VehicleState> vehicleStates = vehicles.stream()
                .map(VehicleState::new)
                .toList();

        // 2. Compute minimum Haversine distance tracking remaining vehicle capacity limits
        for (DeliveryOrder order : sortedOrders) {
            VehicleState bestVehicle = null;
            double shortestDistance = Double.MAX_VALUE;

            for (VehicleState state : vehicleStates) {
                // Skip vehicle instances without adequate remaining package payload space
                if (state.remainingCapacity < order.getPackageWeight()) {
                    continue;
                }

                // Calculate proximity index metrics via Haversine strategy rules
                double distance = HaversineDistanceCalculator.calculateDistanceKm(
                        state.currentLatitude,
                        state.currentLongitude,
                        order.getLatitude(),
                        order.getLongitude());

                // Greedily lock the nearest eligible carrier resource
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    bestVehicle = state;
                }
            }

            // Allocate task details to matching state variables if criteria match perfectly
            if (bestVehicle != null) {
                bestVehicle.assign(order, shortestDistance);
            }
        }

        // 3. Transform functional memory variables into formal specification records
        List<DispatchPlanResponse.VehicleDispatch> dispatchPlanList = vehicleStates.stream()
            .map(vehicleState -> vehicleState.toResponse())
                .toList();

        // Wrap results cleanly matching the target schema layout without metadata inflation
        return new DispatchPlanResponse(dispatchPlanList);
    }

    /**
     * Resolves priority sorting sequences explicitly. Higher rank integers
     * execute later.
     */
    private int priorityRank(Priority priority) {
        return switch (priority) {
            case HIGH ->
                0;
            case MEDIUM ->
                1;
            case LOW ->
                2;
        };
    }

    /**
     * Isolated helper structure tracking state mutations safely during route
     * parsing loops.
     */
    private static class VehicleState {

        private final Vehicle vehicle;
        private double remainingCapacity;
        private double totalLoad;
        private double totalDistanceKm;
        private double currentLatitude;
        private double currentLongitude;
        private final List<DispatchPlanResponse.AssignedOrder> assignedOrders = new ArrayList<>();

        private VehicleState(Vehicle vehicle) {
            this.vehicle = vehicle;
            this.remainingCapacity = vehicle.getCapacity();
            this.currentLatitude = vehicle.getCurrentLatitude();
            this.currentLongitude = vehicle.getCurrentLongitude();
        }

        private void assign(DeliveryOrder order, double distanceKm) {
            remainingCapacity -= order.getPackageWeight();
            totalLoad += order.getPackageWeight();
            totalDistanceKm += distanceKm;

            assignedOrders.add(new DispatchPlanResponse.AssignedOrder(
                    order.getOrderId(),
                    order.getLatitude(),
                    order.getLongitude(),
                    order.getAddress(),
                    order.getPackageWeight(),
                    order.getPriority()));

            // Update local positional references dynamically to set up next leg calculations
            currentLatitude = order.getLatitude();
            currentLongitude = order.getLongitude();
        }

        private DispatchPlanResponse.VehicleDispatch toResponse() {
            // Explicitly round metric variables and format as standard string metrics (e.g., "5 km")
            String formattedDistance = Math.round(totalDistanceKm) + " km";

            return new DispatchPlanResponse.VehicleDispatch(
                    vehicle.getVehicleId(),
                    totalLoad,
                    formattedDistance,
                    List.copyOf(assignedOrders));
        }
    }
}
