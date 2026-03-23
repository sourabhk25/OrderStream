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
public class PaymentEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "payment-events", groupId = "inventory-service-group")
    public void consumePaymentEvent(Map<String, Object> event) {
        String status = (String) event.get("status");

        if ("COMPLETED".equals(status)) {
            String orderNumber = (String) event.get("orderNumber");
            log.info("Payment completed, processing inventory reservation for order: {}", orderNumber);
        }
    }
}
