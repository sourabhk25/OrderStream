package com.orderstream.inventory.consumer;

import com.orderstream.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order-events", groupId = "inventory-service-group")
    public void consumeOrderEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");

        if ("ORDER_CREATED".equals(eventType)) {
            String orderNumber = (String) event.get("orderNumber");
            String productId = (String) event.get("productId");
            Integer quantity = (Integer) event.get("quantity");

            log.info("Reserving inventory for order: {}", orderNumber);
            inventoryService.reserveInventory(orderNumber, productId, quantity);
        }
    }
}
