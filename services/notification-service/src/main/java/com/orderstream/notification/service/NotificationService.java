package com.orderstream.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendOrderCreatedNotification(String orderNumber, String customerId) {
        log.info("📧 Sending order created notification to customer: {} for order: {}",
                customerId, orderNumber);
        simulateEmailSend(customerId, "Order Created",
                "Your order " + orderNumber + " has been created successfully.");
    }

    public void sendPaymentNotification(String orderNumber, String status) {
        log.info("📧 Sending payment {} notification for order: {}", status, orderNumber);
        simulateEmailSend("customer", "Payment " + status,
                "Payment for order " + orderNumber + " is " + status);
    }

    public void sendInventoryNotification(String orderNumber, String status) {
        log.info("📧 Sending inventory {} notification for order: {}", status, orderNumber);
        simulateEmailSend("customer", "Inventory " + status,
                "Inventory status for order " + orderNumber + ": " + status);
    }

    public void sendOrderCompletedNotification(String orderNumber) {
        log.info("📧 Sending order completed notification for order: {}", orderNumber);
        simulateEmailSend("customer", "Order Completed",
                "Your order " + orderNumber + " has been completed and will be shipped soon.");
    }

    private void simulateEmailSend(String to, String subject, String body) {
        log.info("EMAIL SENT - To: {}, Subject: {}, Body: {}", to, subject, body);
    }
}
