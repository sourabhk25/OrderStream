package com.orderstream.order.service;

import com.orderstream.order.dto.OrderEvent;
import com.orderstream.order.dto.OrderRequest;
import com.orderstream.order.model.Order;
import com.orderstream.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Transactional
    public Order createOrder(OrderRequest request) {
        String orderNumber = "ORD-" + UUID.randomUUID().toString();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(request.getTotalAmount())
                .status(Order.OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {}", orderNumber);

        OrderEvent event = OrderEvent.builder()
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(request.getTotalAmount())
                .eventType("ORDER_CREATED")
                .build();

        kafkaTemplate.send("order-events", event);
        log.info("Order event published: {}", orderNumber);

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
    }

    @Transactional
    public void updateOrderStatus(String orderNumber, Order.OrderStatus status) {
        Order order = getOrderByNumber(orderNumber);
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Order status updated: {} -> {}", orderNumber, status);
    }
}
