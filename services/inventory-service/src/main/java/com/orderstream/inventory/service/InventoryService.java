package com.orderstream.inventory.service;

import com.orderstream.inventory.model.Inventory;
import com.orderstream.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Cacheable(value = "inventory", key = "#productId")
    public Inventory getInventory(String productId) {
        log.info("Fetching inventory from DB for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "#productId")
    public boolean reserveInventory(String orderNumber, String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (inventory.getAvailableQuantity() >= quantity) {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);

            log.info("Inventory reserved for order: {} - Product: {} - Quantity: {}",
                    orderNumber, productId, quantity);

            publishInventoryEvent(orderNumber, productId, "RESERVED", quantity);
            return true;
        } else {
            log.warn("Insufficient inventory for order: {} - Product: {} - Required: {} - Available: {}",
                    orderNumber, productId, quantity, inventory.getAvailableQuantity());

            publishInventoryEvent(orderNumber, productId, "INSUFFICIENT", quantity);
            return false;
        }
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "#productId")
    public Inventory createOrUpdateInventory(String productId, String productName, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(Inventory.builder()
                        .productId(productId)
                        .productName(productName)
                        .availableQuantity(0)
                        .reservedQuantity(0)
                        .build());

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

    private void publishInventoryEvent(String orderNumber, String productId, String status, Integer quantity) {
        Map<String, Object> event = new HashMap<>();
        event.put("orderNumber", orderNumber);
        event.put("productId", productId);
        event.put("quantity", quantity);
        event.put("status", status);
        event.put("eventType", "INVENTORY_" + status);

        kafkaTemplate.send("inventory-events", event);
        log.info("Inventory event published for order: {}", orderNumber);
    }
}
