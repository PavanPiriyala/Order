package com.orderservice.sprint4.service;

import com.orderservice.sprint4.dto.*;
import com.orderservice.sprint4.exception.InventoryException;
import com.orderservice.sprint4.exception.OrderNotFoundException;
import com.orderservice.sprint4.exception.UnauthorisedOrderAccessException;
import com.orderservice.sprint4.model.*;
import com.orderservice.sprint4.model.enmus.OrderItemStatus;
import com.orderservice.sprint4.model.enmus.OrderStatus;
import com.orderservice.sprint4.model.enmus.PaymentMode;
import com.orderservice.sprint4.model.enmus.ShipmentItemStatus;
import com.orderservice.sprint4.repository.*;
import com.orderservice.sprint4.security.JwtUtil;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderInvoiceRepository orderInvoiceRepository;
    @Mock
    private ShipmentItemRepository shipmentItemRepository;

    private OrderDetailsRequestDTO orderDetailsRequest;
    private String token = "mock-jwt-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setSku("SKU123");
        item.setProductId(1);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.TEN);
        item.setFinalPrice(new BigDecimal("18"));
        item.setDiscount(new BigDecimal("2"));
        item.setSellerId(99);
        item.setSize("L");

        orderDetailsRequest = new OrderDetailsRequestDTO();
        orderDetailsRequest.setOrderDate(LocalDateTime.now());
        orderDetailsRequest.setOrderTotal(new BigDecimal("20.00"));
        orderDetailsRequest.setPromoDiscount(new BigDecimal("2.00"));
        orderDetailsRequest.setPaymentMode(PaymentMode.valueOf("Card"));
        orderDetailsRequest.setUserId(1001);
        orderDetailsRequest.setOrderItemRequestDTOS(List.of(item));

        when(jwtUtil.getUsernameFromToken(token)).thenReturn("user@example.com");
    }


    @Test
    void testCreateOrderTransaction_Success() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderId(1);
            return order;
        });

        when(orderItemRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<OrderItem> items = invocation.getArgument(0);
            for (int i = 0; i < items.size(); i++) {
                items.get(i).setOrderItemId(i + 1);
            }
            return items;
        });

        doNothing().when(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Void.class));

        when(orderInvoiceRepository.save(any(OrderInvoice.class))).thenReturn(new OrderInvoice());

        when(shipmentItemRepository.save(any(ShipmentItem.class))).thenReturn(new ShipmentItem());

        // Act
        OrderResponseDTO response = orderService.createOrderTransaction(orderDetailsRequest, token);

        // Assert
        assertEquals("success", response.getStatus());
        assertTrue(response.getOrderItemIds().containsKey("SKU123"));

        verify(mailSender).send((MimeMessage) any()); // Email sent
        verify(orderRepository).save(any());
    }

    @Test
    void testCreateOrderTransaction_InventoryFails() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderId(1);
            return order;
        });

        when(orderItemRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<OrderItem> items = invocation.getArgument(0);
            for (int i = 0; i < items.size(); i++) {
                items.get(i).setOrderItemId(i + 1);
            }
            return items;
        });

        doThrow(new RuntimeException("Inventory service down")).when(restTemplate)
                .exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class));

        when(orderInvoiceRepository.save(any())).thenReturn(new OrderInvoice());
        when(shipmentItemRepository.save(any())).thenReturn(new ShipmentItem());

        // Act
        OrderResponseDTO response = orderService.createOrderTransaction(orderDetailsRequest, token);

        // Assert
        assertEquals("failure", response.getStatus());
        verify(mailSender).send((MimeMessage) any()); // Failure email sent
    }

    @Test
    void testGetOrderDetails_Success() {
        Order order = new Order();
        order.setOrderId(1);
        order.setUserId(100);
        order.setOrderStatus(OrderStatus.Ordered);
        order.setOrderDate(LocalDateTime.now());
        order.setPromoDiscount(BigDecimal.TEN);
        order.setOrderTotal(new BigDecimal("100.00"));
        order.setOrderInvoice(new OrderInvoice());
        order.setOrderItems(List.of(new OrderItem()));

        when(jwtUtil.getUserIdFromToken(token)).thenReturn(100);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        OrderDetailsResponseDTO dto = orderService.getOrderDetails(1, token);

        assertEquals(1, dto.getOrderId());
        assertEquals(100, dto.getUserId());
    }

    @Test
    void testGetOrderDetails_Unauthorized() {
        Order order = new Order();
        order.setOrderId(1);
        order.setUserId(101); // mismatch
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(100);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(UnauthorisedOrderAccessException.class,
                () -> orderService.getOrderDetails(1, token));
    }

    @Test
    void testGetOrders_Success() {
        Order order = new Order();
        order.setOrderId(1);
        order.setUserId(100);
        order.setOrderDate(LocalDateTime.now().minusDays(5));
        order.setOrderStatus(OrderStatus.Ordered);
        order.setOrderTotal(new BigDecimal("200"));
        order.setOrderItems(List.of(new OrderItem()));

        when(jwtUtil.getUserIdFromToken(token)).thenReturn(100);
        doNothing().when(restTemplate).getForEntity(anyString(), eq(UserBasicInfoResponse.class));

        when(orderRepository.findRecentOrdersByUserId(eq(100), any()))
                .thenReturn(List.of(order));

        List<OrderSummaryDTO> summaries = orderService.getOrders(1, token);
        assertEquals(1, summaries.size());
        assertEquals(1L, summaries.get(0).getItems());
    }

    @Test
    void testGetOrders_UserNotFound() {
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(100);
        when(restTemplate.getForEntity(anyString(), eq(UserBasicInfoResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            orderService.getOrders(1, token);
        });

        assertTrue(ex.getMessage().contains("User with ID 100 does not exist"));
    }

    @Test
    void testGetOrders_NoOrders() {
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(100);
        doNothing().when(restTemplate).getForEntity(anyString(), eq(UserBasicInfoResponse.class));

        when(orderRepository.findRecentOrdersByUserId(eq(100), any()))
                .thenReturn(Collections.emptyList());

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrders(1, token));

        assertEquals("No recent orders found for user ID: 100", ex.getMessage());
    }
}
