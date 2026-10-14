
package com.dispatch.repository;

import com.dispatch.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository
        extends JpaRepository<DeliveryOrder, String> {
}