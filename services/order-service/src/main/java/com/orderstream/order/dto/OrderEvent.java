package com.orderstream.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderNumber;
    private String customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String eventType;
}
