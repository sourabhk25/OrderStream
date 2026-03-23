package com.orderstream.order.service;

import com.orderstream.order.dto.OrderEvent;
import com.orderstream.order.dto.OrderRequest;
import com.orderstream.order.model.Order;
import com.orderstream.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-test-1234")
                .customerId("CUST-001")
                .productId("PROD-001")
                .quantity(2)
                .totalAmount(new BigDecimal("1999.99"))
                .status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createOrder — persists order and publishes ORDER_CREATED Kafka event")
    void createOrder_shouldSaveOrderAndPublishKafkaEvent() {
        // Arrange
        OrderRequest request = new OrderRequest("CUST-001", "PROD-001", 2, new BigDecimal("1999.99"));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        // Act
        Order result = orderService.createOrder(request);

        // Assert — order was saved
        verify(orderRepository, times(1)).save(any(Order.class));

        // Assert — Kafka event published to correct topic
        ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(kafkaTemplate, times(1)).send(eq("order-events"), eventCaptor.capture());

        OrderEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(publishedEvent.getCustomerId()).isEqualTo("CUST-001");
        assertThat(publishedEvent.getProductId()).isEqualTo("PROD-001");
        assertThat(publishedEvent.getQuantity()).isEqualTo(2);
        assertThat(publishedEvent.getTotalAmount()).isEqualByComparingTo("1999.99");
        assertThat(publishedEvent.getOrderNumber()).startsWith("ORD-");

        // Assert — returned order is the saved entity
        assertThat(result.getOrderNumber()).isEqualTo("ORD-test-1234");
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    @Test
    @DisplayName("createOrder — order number is auto-generated with ORD- prefix")
    void createOrder_shouldGenerateOrderNumberWithPrefix() {
        OrderRequest request = new OrderRequest("CUST-002", "PROD-002", 1, new BigDecimal("99.99"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(request);

        assertThat(result.getOrderNumber()).startsWith("ORD-");
        assertThat(result.getOrderNumber()).hasSizeGreaterThan(4);
    }

    @Test
    @DisplayName("getAllOrders — returns all orders from repository")
    void getAllOrders_shouldReturnAllOrders() {
        Order order2 = Order.builder()
                .id(2L)
                .orderNumber("ORD-test-5678")
                .customerId("CUST-002")
                .productId("PROD-002")
                .quantity(1)
                .totalAmount(new BigDecimal("499.99"))
                .status(Order.OrderStatus.PAYMENT_COMPLETED)
                .build();

        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder, order2));

        List<Order> results = orderService.getAllOrders();

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Order::getCustomerId)
                .containsExactlyInAnyOrder("CUST-001", "CUST-002");
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getOrderByNumber — returns order when found")
    void getOrderByNumber_shouldReturnOrder_whenExists() {
        when(orderRepository.findByOrderNumber("ORD-test-1234"))
                .thenReturn(Optional.of(sampleOrder));

        Order result = orderService.getOrderByNumber("ORD-test-1234");

        assertThat(result.getOrderNumber()).isEqualTo("ORD-test-1234");
        assertThat(result.getCustomerId()).isEqualTo("CUST-001");
        verify(orderRepository, times(1)).findByOrderNumber("ORD-test-1234");
    }

    @Test
    @DisplayName("getOrderByNumber — throws RuntimeException when order not found")
    void getOrderByNumber_shouldThrowException_whenNotFound() {
        when(orderRepository.findByOrderNumber("ORD-UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByNumber("ORD-UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("updateOrderStatus — persists new status on existing order")
    void updateOrderStatus_shouldSaveUpdatedStatus() {
        when(orderRepository.findByOrderNumber("ORD-test-1234"))
                .thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        orderService.updateOrderStatus("ORD-test-1234", Order.OrderStatus.PAYMENT_COMPLETED);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_COMPLETED);
    }

    @Test
    @DisplayName("updateOrderStatus — throws when order number does not exist")
    void updateOrderStatus_shouldThrow_whenOrderNotFound() {
        when(orderRepository.findByOrderNumber("ORD-GHOST"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.updateOrderStatus("ORD-GHOST", Order.OrderStatus.PAYMENT_FAILED))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }
}
