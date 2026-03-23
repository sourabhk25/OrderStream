package com.orderstream.payment.consumer;

import com.orderstream.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void consumeOrderEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");

        if ("ORDER_CREATED".equals(eventType)) {
            String orderNumber = (String) event.get("orderNumber");
            String customerId = (String) event.get("customerId");
            BigDecimal totalAmount = new BigDecimal(event.get("totalAmount").toString());

            log.info("Processing payment for order: {}", orderNumber);
            paymentService.processPayment(orderNumber, customerId, totalAmount);
        }
    }
}
