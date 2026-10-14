package com.dispatch.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dispatch.dto.OrderRequest;
import com.dispatch.entity.DeliveryOrder;
import com.dispatch.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void saveOrders(List<OrderRequest> requests) {
        // Guard clause to handle empty or invalid payloads gracefully
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<DeliveryOrder> orders = requests.stream()
                .map(request -> new DeliveryOrder(
                        request.orderId(),
                        request.latitude(),
                        request.longitude(),
                        request.address(),
                        request.packageWeight(),
                        request.priority()))
                .toList();

        orderRepository.saveAll(orders);
    }
}
