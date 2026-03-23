package com.orderstream.notification.consumer;

import com.orderstream.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void consumeOrderEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        String orderNumber = (String) event.get("orderNumber");
        String customerId = (String) event.get("customerId");

        log.info("Received order event: {} for order: {}", eventType, orderNumber);

        if ("ORDER_CREATED".equals(eventType)) {
            notificationService.sendOrderCreatedNotification(orderNumber, customerId);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void consumePaymentEvent(Map<String, Object> event) {
        String orderNumber = (String) event.get("orderNumber");
        String status = (String) event.get("status");

        log.info("Received payment event for order: {} with status: {}", orderNumber, status);
        notificationService.sendPaymentNotification(orderNumber, status);
    }

    @KafkaListener(topics = "inventory-events", groupId = "notification-service-group")
    public void consumeInventoryEvent(Map<String, Object> event) {
        String orderNumber = (String) event.get("orderNumber");
        String status = (String) event.get("status");

        log.info("Received inventory event for order: {} with status: {}", orderNumber, status);
        notificationService.sendInventoryNotification(orderNumber, status);

        if ("RESERVED".equals(status)) {
            notificationService.sendOrderCompletedNotification(orderNumber);
        }
    }
}
