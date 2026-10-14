package com.dispatch.optimizer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import com.dispatch.dto.DispatchPlanResponse;
import com.dispatch.dto.DispatchPlanResponse.VehicleDispatch;
import com.dispatch.entity.DeliveryOrder;
import com.dispatch.entity.Priority;
import com.dispatch.entity.Vehicle;

class GreedyDispatchOptimizerTest {

    private final GreedyDispatchOptimizer optimizer = new GreedyDispatchOptimizer();

    /**
     * 1. SAFETIES & DEFENSIVE GUARDS: Null Argument Assertions
     */
    @Test
    void testOptimizeWithNullArgumentsThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> optimizer.optimize(null, new ArrayList<>()));
        assertNotNull(exception);

        IllegalArgumentException secondException = assertThrows(
                IllegalArgumentException.class, () -> optimizer.optimize(new ArrayList<>(), null));
        assertNotNull(secondException);
    }

    /**
     * 2. SAFETIES & DEFENSIVE GUARDS: Empty Collections Parsing
     */
    @Test
    void testEmptyFleetOrOrdersReturnsGracefully() {
        Vehicle vehicle = new Vehicle("VEH-01", 100.0, 12.9716, 77.5946, "Depot A");

        // Empty orders array handling check
        DispatchPlanResponse emptyOrdersResponse = optimizer.optimize(Collections.emptyList(), List.of(vehicle));
        assertNotNull(emptyOrdersResponse);
        assertEquals(1, emptyOrdersResponse.dispatchPlan().size());
        assertEquals("VEH-01", emptyOrdersResponse.dispatchPlan().get(0).vehicleId());
        assertEquals(0.0, emptyOrdersResponse.dispatchPlan().get(0).totalLoad());
        assertEquals("0 km", emptyOrdersResponse.dispatchPlan().get(0).totalDistance());

        // Empty vehicle fleet list handling check
        DispatchPlanResponse emptyFleetResponse = optimizer.optimize(
                List.of(new DeliveryOrder("ORD-01", 12.9716, 77.5946, "A1", 10.0, Priority.HIGH)),
                Collections.emptyList()
        );
        assertNotNull(emptyFleetResponse);
        assertTrue(emptyFleetResponse.dispatchPlan().isEmpty());
    }

    /**
     * 3. MANDATORY ALGO CONSTRAINT: Priority Dominance Tiering Verifies that
     * HIGH priority orders are picked first, even if a LOW priority order is
     * sitting right at the vehicle's location (0 km distance away).
     */
    @Test
    void testPriorityTieringTakesPrecedenceOverDistance() {
        Vehicle vehicle = new Vehicle("VEH-01", 100.0, 12.0, 77.0, "Start Node");

        DeliveryOrder lowPriorityClose = new DeliveryOrder("ORD-LOW", 12.0, 77.0, "Close Address", 10.0, Priority.LOW);
        DeliveryOrder highPriorityFar = new DeliveryOrder("ORD-HIGH", 13.0, 78.0, "Far Address", 20.0, Priority.HIGH);

        DispatchPlanResponse response = optimizer.optimize(List.of(lowPriorityClose, highPriorityFar), List.of(vehicle));

        VehicleDispatch dispatch = response.dispatchPlan().get(0);
        assertEquals(2, dispatch.assignedOrders().size());

        // High priority must sit at index 0 because it was prioritized before distance calculations
        assertEquals("ORD-HIGH", dispatch.assignedOrders().get(0).orderId());
        assertEquals("ORD-LOW", dispatch.assignedOrders().get(1).orderId());
    }

    /**
     * 4. DETERMINISTIC SORTING: Lexicographical Tie-Breaker Verifies that when
     * priority levels match exactly, orders are sorted deterministically using
     * their unique string IDs to guarantee predictable output patterns.
     */
    @Test
    void testDeterministicTieBreakerForSamePriority() {
        Vehicle vehicle = new Vehicle("VEH-01", 100.0, 12.9716, 77.5946, "Depot A");

        DeliveryOrder orderB = new DeliveryOrder("ORD-B", 12.9716, 77.5946, "Addr B", 10.0, Priority.HIGH);
        DeliveryOrder orderA = new DeliveryOrder("ORD-A", 12.9716, 77.5946, "Addr A", 15.0, Priority.HIGH);

        // Input lists in random/reverse order to test strict key evaluation sorting
        DispatchPlanResponse response = optimizer.optimize(List.of(orderB, orderA), List.of(vehicle));

        VehicleDispatch dispatch = response.dispatchPlan().get(0);
        assertEquals("ORD-A", dispatch.assignedOrders().get(0).orderId());
        assertEquals("ORD-B", dispatch.assignedOrders().get(1).orderId());
    }

    /**
     * 5. CRITICAL CRITERIA EDGE CASE: Order Exceeds Entire Fleet Capacities
     * Ensures an oversized order doesn't throw runtime failures or skip
     * processing valid lower priority orders that fit within capacity.
     */
    @Test
    void testOrderWeightExceedsAllFleetCapacities() {
        Vehicle vehicle = new Vehicle("VEH-01", 50.0, 12.9716, 77.5946, "Depot A");

        DeliveryOrder highNormal = new DeliveryOrder("ORD-01", 12.9716, 77.5946, "Addr 1", 20.0, Priority.HIGH);
        DeliveryOrder massive = new DeliveryOrder("ORD-OVER", 12.9716, 77.5946, "Addr 2", 999.0, Priority.HIGH);
        DeliveryOrder mediumNormal = new DeliveryOrder("ORD-02", 12.9716, 77.5946, "Addr 3", 15.0, Priority.MEDIUM);

        DispatchPlanResponse response = optimizer.optimize(List.of(highNormal, massive, mediumNormal), List.of(vehicle));

        VehicleDispatch vehiclePlan = response.dispatchPlan().get(0);
        assertEquals(35.0, vehiclePlan.totalLoad());
        assertEquals(2, vehiclePlan.assignedOrders().size());

        boolean containsMassive = vehiclePlan.assignedOrders().stream().anyMatch(o -> "ORD-OVER".equals(o.orderId()));
        assertFalse(containsMassive, "Overweight payload must be entirely bypassed by allocation logic loop structures.");
    }

    /**
     * 6. BOUNDARY VALUE EDGE CASE: Exact Structural Capacity Match Confirms
     * boundary calculations process assignments safely when package weights
     * exactly match remaining capacity limits down to decimal baselines.
     */
    @Test
    void testOrderWeightExactlyEqualsRemainingCapacity() {
        Vehicle vehicle = new Vehicle("VEH-01", 30.0, 12.9716, 77.5946, "Depot A");
        DeliveryOrder exactWeightOrder = new DeliveryOrder("ORD-EXACT", 12.9716, 77.5946, "Addr 1", 30.0, Priority.HIGH);

        DispatchPlanResponse response = optimizer.optimize(List.of(exactWeightOrder), List.of(vehicle));

        VehicleDispatch vehiclePlan = response.dispatchPlan().get(0);
        assertEquals(30.0, vehiclePlan.totalLoad());
        assertEquals(1, vehiclePlan.assignedOrders().size());
        assertEquals("ORD-EXACT", vehiclePlan.assignedOrders().get(0).orderId());
    }

    /**
     * 7. GEOGRAPHIC ROUTING CONSTRAINT: Nearest Carrier Resource Match Confirms
     * optimization logic links order nodes with the closest vehicle via
     * Haversine distance computations.
     */
    @Test
    void testOrderAssignedToGeographicallyNearestEligibleVehicle() {
        Vehicle nearVehicle = new Vehicle("VEH-NEAR", 100.0, 12.9716, 77.5946, "Bangalore Depot");
        Vehicle farVehicle = new Vehicle("VEH-FAR", 100.0, 13.0827, 80.2707, "Chennai Depot");

        DeliveryOrder bangaloreOrder = new DeliveryOrder("ORD-BLR", 12.9716, 77.5946, "MG Road, Bangalore", 10.0, Priority.HIGH);

        DispatchPlanResponse response = optimizer.optimize(List.of(bangaloreOrder), List.of(nearVehicle, farVehicle));

        VehicleDispatch nearPlan = response.dispatchPlan().stream()
                .filter(v -> "VEH-NEAR".equals(v.vehicleId())).findFirst().orElseThrow();
        VehicleDispatch farPlan = response.dispatchPlan().stream()
                .filter(v -> "VEH-FAR".equals(v.vehicleId())).findFirst().orElseThrow();

        assertEquals(1, nearPlan.assignedOrders().size());
        assertEquals("ORD-BLR", nearPlan.assignedOrders().get(0).orderId());
        assertTrue(farPlan.assignedOrders().isEmpty());
    }

    /**
     * 8. COMPLEX EDGE CASE: Sequential Positional Traversal Updating Verifies
     * that after a vehicle accepts an order, its location updates to that
     * order's drop point. The next distance calculation must start from this
     * new point, not the starting depot.
     */
    @Test
    void testVehicleTrackingUpdatesPositionToLastAssignedOrderDestination() {
        Vehicle vehicle = new Vehicle("VEH-01", 100.0, 0.0, 0.0, "Depot");

        DeliveryOrder order1 = new DeliveryOrder("ORD-01", 1.0, 0.0, "Stop 1", 10.0, Priority.HIGH);
        DeliveryOrder order2 = new DeliveryOrder("ORD-02", 2.0, 0.0, "Stop 2", 10.0, Priority.HIGH);

        DispatchPlanResponse response = optimizer.optimize(List.of(order1, order2), List.of(vehicle));

        VehicleDispatch vehiclePlan = response.dispatchPlan().get(0);
        assertEquals(2, vehiclePlan.assignedOrders().size());

        String totalDistanceStr = vehiclePlan.totalDistance();
        long numericDistance = Long.parseLong(totalDistanceStr.replace(" km", ""));

        // Hop-by-hop (0->1->2) is ~222km. If it reset to depot each time (0->1, 0->2), it would be ~333km.
        assertTrue(numericDistance < 300, "Distance tracking must dynamically step forward sequentially along drop locations.");
    }

    /**
     * 9. API SCHEMA SPEC COMPLIANCE: Zero Metric Preservation for Unused Fleets
     * Ensures that completely idle vehicles are still returned in the JSON
     * array schema with zero-values initialized, as shown in the requirement
     * specifications.
     */
    @Test
    void testUnusedVehiclesIncludedInResponseWithZeroMetrics() {
        Vehicle unusedVehicle = new Vehicle("VEH-EMPTY", 100.0, 12.9716, 77.5946, "Depot A");
        DeliveryOrder massiveOrder = new DeliveryOrder("ORD-OVER", 12.9716, 77.5946, "Addr 1", 999.0, Priority.HIGH);

        DispatchPlanResponse response = optimizer.optimize(List.of(massiveOrder), List.of(unusedVehicle));

        assertNotNull(response);
        assertEquals(1, response.dispatchPlan().size());
        VehicleDispatch unusedPlan = response.dispatchPlan().get(0);
        assertEquals("VEH-EMPTY", unusedPlan.vehicleId());
        assertEquals(0.0, unusedPlan.totalLoad());
        assertEquals("0 km", unusedPlan.totalDistance());
        assertTrue(unusedPlan.assignedOrders().isEmpty());
    }
}
