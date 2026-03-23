package com.orderstream.order.consumer;

import com.orderstream.order.model.Order;
import com.orderstream.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void consumePaymentEvent(Map<String, Object> event) {
        String orderNumber = (String) event.get("orderNumber");
        String status = (String) event.get("status");

        log.info("Received payment event for order: {} with status: {}", orderNumber, status);

        if ("COMPLETED".equals(status)) {
            orderService.updateOrderStatus(orderNumber, Order.OrderStatus.PAYMENT_COMPLETED);
        } else if ("FAILED".equals(status)) {
            orderService.updateOrderStatus(orderNumber, Order.OrderStatus.PAYMENT_FAILED);
        }
    }
}
