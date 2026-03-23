package com.orderstream.payment.service;

import com.orderstream.payment.model.Payment;
import com.orderstream.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Transactional
    public Payment processPayment(String orderNumber, String customerId, BigDecimal amount) {
        String transactionId = "TXN-" + UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .orderNumber(orderNumber)
                .customerId(customerId)
                .amount(amount)
                .status(Payment.PaymentStatus.PROCESSING)
                .paymentMethod("CREDIT_CARD")
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processing started: {}", transactionId);

        boolean paymentSuccessful = simulatePaymentProcessing(amount);

        if (paymentSuccessful) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            log.info("Payment completed: {}", transactionId);

            publishPaymentEvent(orderNumber, "COMPLETED", transactionId);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.warn("Payment failed: {}", transactionId);

            publishPaymentEvent(orderNumber, "FAILED", transactionId);
        }

        return payment;
    }

    private boolean simulatePaymentProcessing(BigDecimal amount) {
        return amount.compareTo(BigDecimal.valueOf(10000)) < 0;
    }

    private void publishPaymentEvent(String orderNumber, String status, String transactionId) {
        Map<String, Object> event = new HashMap<>();
        event.put("orderNumber", orderNumber);
        event.put("transactionId", transactionId);
        event.put("status", status);
        event.put("eventType", "PAYMENT_" + status);

        kafkaTemplate.send("payment-events", event);
        log.info("Payment event published for order: {}", orderNumber);
    }
}
