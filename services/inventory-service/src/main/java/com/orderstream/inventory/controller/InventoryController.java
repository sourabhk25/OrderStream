package com.orderstream.inventory.controller;

import com.orderstream.inventory.model.Inventory;
import com.orderstream.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(
            @RequestParam String productId,
            @RequestParam String productName,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.createOrUpdateInventory(productId, productName, quantity));
    }
}
