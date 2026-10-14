
package com.dispatch.optimizer;

import java.util.List;

import com.dispatch.dto.DispatchPlanResponse;
import com.dispatch.entity.DeliveryOrder;
import com.dispatch.entity.Vehicle;

public interface DispatchOptimizer {

    DispatchPlanResponse optimize(
            List<DeliveryOrder> orders,
            List<Vehicle> vehicles);
}
