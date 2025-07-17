package com.orderservice.sprint4.service;

import com.orderservice.sprint4.dao.ShipmentItemDAO;
import com.orderservice.sprint4.dto.*;
import com.orderservice.sprint4.exception.InvoiceNotFoundException;
import com.orderservice.sprint4.exception.OrderNotFoundException;
import com.orderservice.sprint4.exception.ShipmentItemsNotFoundException;
import com.orderservice.sprint4.model.Order;
import com.orderservice.sprint4.model.enmus.OrderStatus;
import com.orderservice.sprint4.model.enmus.PaymentMode;
import com.orderservice.sprint4.model.enmus.ShipmentItemStatus;
import com.orderservice.sprint4.repository.OrderInvoiceRepository;
import com.orderservice.sprint4.repository.OrderRepository;
import com.orderservice.sprint4.repository.custom.CustomShipmentItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentServiceImplTest {

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderInvoiceRepository orderInvoiceRepository;

    @Mock
    private CustomShipmentItemRepository customShipmentItemRepository;

    @Mock
    private RestTemplate restTemplate;

    private Order order;
    private ShipmentItemDAO itemDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        order = new Order();
        order.setOrderId(1);
        order.setOrderDate(LocalDateTime.of(2025, 7, 15, 10, 0));
        order.setOrderStatus(OrderStatus.Ordered);

        itemDAO = new ShipmentItemDAO();
        itemDAO.setProduct(101);
        itemDAO.setQuantity(2);
        itemDAO.setItemStatus(ShipmentItemStatus.InTransit);
        itemDAO.setSku("SKU123");
        itemDAO.setTrackingId("TRK123");
        itemDAO.setDeliveredDate(LocalDateTime.now().plusDays(3));
        itemDAO.setShipmentDate(LocalDateTime.now());
        itemDAO.setSeller(99);
    }

    @Test
    void testGetShipmentItemsByOrderId_Success() {
        int orderId = 1;

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(customShipmentItemRepository.getShipmentItemsByOrderId(orderId)).thenReturn(List.of(itemDAO));

        // Mock product & seller responses
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");

        SellerDTO seller = new SellerDTO();
        seller.setSellerName("Test Seller");

        when(restTemplate.getForEntity(contains("/" + itemDAO.getProduct()), eq(ProductDTO.class)))
                .thenReturn(new ResponseEntity<>(product, HttpStatus.OK));

        when(restTemplate.getForEntity(contains("/" + itemDAO.getProduct() + "/sellers"), eq(SellerDTO.class)))
                .thenReturn(new ResponseEntity<>(seller, HttpStatus.OK));

        InvoiceResponseDTO invoice = new InvoiceResponseDTO();
        invoice.setPaymentMode(PaymentMode.valueOf("Card"));
        when(orderInvoiceRepository.getInvoiceByOrderId(orderId)).thenReturn(invoice);

        // Act
        ShipmentDetailsResponseDTO response = shipmentService.getShipmentItemsByOrderId(orderId);

        // Assert basic values
        assertNotNull(response);
        assertEquals(order.getOrderId(), response.getOrderId());
        assertEquals(order.getOrderStatus(), response.getOrderStatus());
        assertEquals("Card", response.getPaymentMode());
        assertEquals(1, response.getItems().size());

        ShipmentItemDTO item = response.getItems().get(0);
        assertEquals("Test Product", item.getProduct());
        assertEquals("Test Seller", item.getSeller());
        assertEquals("TRK123", item.getTrackingId());
    }

    @Test
    void testGetShipmentItemsByOrderId_OrderNotFound() {
        when(orderRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class,
                () -> shipmentService.getShipmentItemsByOrderId(1));
    }

    @Test
    void testGetShipmentItemsByOrderId_NoItems() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(customShipmentItemRepository.getShipmentItemsByOrderId(1)).thenReturn(List.of());

        assertThrows(ShipmentItemsNotFoundException.class,
                () -> shipmentService.getShipmentItemsByOrderId(1));
    }

    @Test
    void testGetShipmentItemsByOrderId_NoInvoice() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(customShipmentItemRepository.getShipmentItemsByOrderId(1)).thenReturn(List.of(itemDAO));

        // Mock REST services
        when(restTemplate.getForEntity(contains("/" + itemDAO.getProduct()), eq(ProductDTO.class)))
                .thenReturn(ResponseEntity.ok(new ProductDTO()));
        when(restTemplate.getForEntity(contains("/" + itemDAO.getProduct() + "/sellers"), eq(SellerDTO.class)))
                .thenReturn(ResponseEntity.ok(new SellerDTO()));

        when(orderInvoiceRepository.getInvoiceByOrderId(1)).thenReturn(null);

        assertThrows(InvoiceNotFoundException.class, () -> shipmentService.getShipmentItemsByOrderId(1));
    }
}
