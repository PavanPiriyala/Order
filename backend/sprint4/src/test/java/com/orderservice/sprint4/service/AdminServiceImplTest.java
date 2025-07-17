package com.orderservice.sprint4.service;


import com.orderservice.sprint4.dto.OrderItemStatusRequestDTO;
import com.orderservice.sprint4.dto.OrderStageDTO;
import com.orderservice.sprint4.dto.ShipmentItemListDTO;
import com.orderservice.sprint4.exception.OrderNotFoundException;
import com.orderservice.sprint4.model.OrderItem;
import com.orderservice.sprint4.model.ShipmentItem;
import com.orderservice.sprint4.model.enmus.ShipmentItemStatus;
import com.orderservice.sprint4.repository.OrderRepository;
import com.orderservice.sprint4.repository.ShipmentItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ShipmentItemRepository shipmentItemRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private OrderItemStatusRequestDTO dto;

    @BeforeEach
    void setup() {
        dto = new OrderItemStatusRequestDTO();
    }

    // ------- orderShipped() -------
    @Test
    void testOrderShipped_Success() {
        dto.setItemStatus(ShipmentItemStatus.InTransit);
        dto.setOrderItemId(1);

        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemStatus(ShipmentItemStatus.Pending);
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(1);
        shipmentItem.setOrderItem(orderItem);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(shipmentItem);
        when(shipmentItemRepository.save(any(ShipmentItem.class))).thenReturn(shipmentItem);
        when(restTemplate.postForObject(anyString(), any(OrderStageDTO.class), eq(String.class))).thenReturn("OK");

        assertDoesNotThrow(() -> adminService.orderShipped(dto));
        verify(shipmentItemRepository).save(shipmentItem);
        verify(restTemplate).postForObject(anyString(), any(OrderStageDTO.class), eq(String.class));
    }

    @Test
    void testOrderShipped_WrongInput() {
        dto.setItemStatus(ShipmentItemStatus.Pending);
        dto.setOrderItemId(1);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.orderShipped(dto));
        assertEquals("Wrong Input", ex.getMessage());
    }

    @Test
    void testOrderShipped_ItemNotFound() {
        dto.setItemStatus(ShipmentItemStatus.InTransit);
        dto.setOrderItemId(1);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(null);
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class, () -> adminService.orderShipped(dto));
        assertEquals("Order with ID 1 not found", ex.getMessage());

    }

    @Test
    void testOrderShipped_ItemAlreadyUpdated() {
        dto.setItemStatus(ShipmentItemStatus.InTransit);
        dto.setOrderItemId(1);

        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemStatus(ShipmentItemStatus.InTransit);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(shipmentItem);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.orderShipped(dto));
        assertEquals("Item already uptodate", ex.getMessage());
    }

    @Test
    void testOrderShipped_InventoryServiceDown() {
        dto.setItemStatus(ShipmentItemStatus.InTransit);
        dto.setOrderItemId(1);

        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemStatus(ShipmentItemStatus.Pending);
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(1);
        shipmentItem.setOrderItem(orderItem);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(shipmentItem);
        when(shipmentItemRepository.save(any(ShipmentItem.class))).thenReturn(shipmentItem);
        when(restTemplate.postForObject(anyString(), any(OrderStageDTO.class), eq(String.class)))
                .thenThrow(new RuntimeException("Service down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.orderShipped(dto));
        assertEquals("Inventory Service down", ex.getMessage());
    }

    // ------- orderDelivered() -------
    @Test
    void testOrderDelivered_Success() {
        dto.setItemStatus(ShipmentItemStatus.Delivered);
        dto.setOrderItemId(1);

        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemStatus(ShipmentItemStatus.InTransit);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(shipmentItem);
        when(shipmentItemRepository.save(any(ShipmentItem.class))).thenReturn(shipmentItem);

        assertDoesNotThrow(() -> adminService.orderDelivered(dto));
        verify(shipmentItemRepository).save(shipmentItem);
    }

    @Test
    void testOrderDelivered_WrongInput() {
        dto.setItemStatus(ShipmentItemStatus.Pending);
        dto.setOrderItemId(1);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.orderDelivered(dto));
        assertEquals("Wrong Input", ex.getMessage());
    }

    @Test
    void testOrderDelivered_ItemNotFound() {
        dto.setItemStatus(ShipmentItemStatus.Delivered);
        dto.setOrderItemId(1);

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class, () -> adminService.orderShipped(dto));
        assertEquals("Order with ID 1 not found", ex.getMessage());

    }

    @Test
    void testOrderDelivered_OrderIsUptodate() {
        dto.setItemStatus(ShipmentItemStatus.Delivered);
        dto.setOrderItemId(1);

        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemStatus(ShipmentItemStatus.Delivered);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(shipmentItem);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.orderDelivered(dto));
        assertEquals("Order is uptodate", ex.getMessage());
    }

    // ------- getShipmentByStatus() -------
    @Test
    void testGetShipmentByStatus_Success() {
        ShipmentItem item1 = new ShipmentItem();
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrderItemId(1);
        com.orderservice.sprint4.model.Order order1 = new com.orderservice.sprint4.model.Order();
        order1.setOrderId(100);
        orderItem1.setOrder(order1);
        item1.setOrderItem(orderItem1);
        item1.setItemStatus(ShipmentItemStatus.Pending);
        item1.setShipmentDate(LocalDateTime.now());

        ShipmentItem item2 = new ShipmentItem();
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setOrderItemId(2);
        com.orderservice.sprint4.model.Order order2 = new com.orderservice.sprint4.model.Order();
        order2.setOrderId(101);
        orderItem2.setOrder(order2);
        item2.setOrderItem(orderItem2);
        item2.setItemStatus(ShipmentItemStatus.Pending);
        item2.setShipmentDate(LocalDateTime.now());

        when(shipmentItemRepository.findByItemStatus(ShipmentItemStatus.Pending)).thenReturn(Arrays.asList(item1, item2));
        List<ShipmentItemListDTO> result = adminService.getShipmentByStatus(ShipmentItemStatus.Pending);
        assertEquals(2, result.size());
        assertEquals(100, result.get(0).getOrderId());
        assertEquals(1, result.get(0).getOrderItemId());
        assertEquals(ShipmentItemStatus.Pending, result.get(0).getItemStatus());
    }

    @Test
    void testGetShipmentByStatus_WrongInput() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.getShipmentByStatus(null));
        assertEquals("Wrong Input", ex.getMessage());
    }

    @Test
    void testGetShipmentByStatus_ItemsNotFound() {
        when(shipmentItemRepository.findByItemStatus(ShipmentItemStatus.Pending)).thenReturn(Collections.emptyList());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.getShipmentByStatus(ShipmentItemStatus.Pending));
        assertEquals("Items not found", ex.getMessage());
    }

    // ------- orderCanelled() -------
    @Test
    void testOrderCanelled_Success() {
        dto.setItemStatus(ShipmentItemStatus.Cancelled);
        dto.setOrderItemId(1);

        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemStatus(ShipmentItemStatus.Pending);
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(1);
        shipmentItem.setOrderItem(orderItem);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(shipmentItem);
        when(shipmentItemRepository.save(any(ShipmentItem.class))).thenReturn(shipmentItem);
        when(restTemplate.postForObject(anyString(), any(OrderStageDTO.class), eq(String.class))).thenReturn("OK");

        assertDoesNotThrow(() -> adminService.orderCanelled(dto));
        verify(shipmentItemRepository).save(shipmentItem);
        verify(restTemplate).postForObject(anyString(), any(OrderStageDTO.class), eq(String.class));
    }

    @Test
    void testOrderCanelled_WrongInput() {
        dto.setItemStatus(ShipmentItemStatus.Pending);
        dto.setOrderItemId(1);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.orderCanelled(dto));
        assertEquals("Wrong Input", ex.getMessage());
    }

    @Test
    void testOrderCanelled_ItemNotFound() {
        dto.setItemStatus(ShipmentItemStatus.Cancelled);
        dto.setOrderItemId(1);
        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(null);
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class, () -> adminService.orderShipped(dto));
        assertEquals("Order with ID 1 not found", ex.getMessage());

    }

    @Test
    void testOrderCanelled_OrderIsUptodate() {
        dto.setItemStatus(ShipmentItemStatus.Pending);
        dto.setOrderItemId(1);
        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemStatus(ShipmentItemStatus.InTransit);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(shipmentItem);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.orderCanelled(dto));
        assertEquals("Order is uptodate", ex.getMessage());
    }

    @Test
    void testOrderCanelled_InventoryServiceDown() {
        dto.setItemStatus(ShipmentItemStatus.Cancelled);
        dto.setOrderItemId(1);

        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemStatus(ShipmentItemStatus.Pending);
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(1);
        shipmentItem.setOrderItem(orderItem);

        when(shipmentItemRepository.findByOrderItemOrderItemId(1)).thenReturn(shipmentItem);
        when(shipmentItemRepository.save(any(ShipmentItem.class))).thenReturn(shipmentItem);
        when(restTemplate.postForObject(anyString(), any(OrderStageDTO.class), eq(String.class)))
                .thenThrow(new RuntimeException("Service down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.orderCanelled(dto));
        assertEquals("Inventory Service down", ex.getMessage());
    }
}
