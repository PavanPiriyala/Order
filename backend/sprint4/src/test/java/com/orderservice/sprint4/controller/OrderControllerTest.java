package com.orderservice.sprint4.controller;

import com.orderservice.sprint4.dto.*;
import com.orderservice.sprint4.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getHeader("Authorization")).thenReturn("Bearer testToken123");
    }

    @Test
    void createOrder_success() {
        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO();
        OrderResponseDTO responseDTO = OrderResponseDTO.builder()
                .orderItemIds(Collections.singletonMap("sku", "id"))
                .status("success")
                .build();

        when(orderService.createOrderTransaction(eq(dto), eq("testToken123"))).thenReturn(responseDTO);

        ResponseEntity<OrderResponseDTO> response = orderController.createOrder(dto, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody());
        verify(orderService).createOrderTransaction(dto, "testToken123");
    }

    @Test
    void createOrder_error_returnsFailureResponse() {
        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO();
        when(orderService.createOrderTransaction(any(), any())).thenThrow(new RuntimeException("fail"));

        ResponseEntity<OrderResponseDTO> response = orderController.createOrder(dto, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("failure", response.getBody().getStatus());
        assertTrue(response.getBody().getOrderItemIds().isEmpty());
    }

    @Test
    void getOrderDetails_success() {
        OrderDetailsResponseDTO detailsDTO = new OrderDetailsResponseDTO();
        when(orderService.getOrderDetails(eq(10), eq("testToken123"))).thenReturn(detailsDTO);

        ResponseEntity<OrderDetailsResponseDTO> response = orderController.getOrderDetails(10, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(detailsDTO, response.getBody());
        verify(orderService).getOrderDetails(10, "testToken123");
    }

    @Test
    void getOrderDetails_notfound_returns404() {
        when(orderService.getOrderDetails(eq(10), eq("testToken123"))).thenThrow(new RuntimeException("not found"));
        ResponseEntity<OrderDetailsResponseDTO> response = orderController.getOrderDetails(10, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getOrdersList_success() {
        List<OrderSummaryDTO> list = List.of(new OrderSummaryDTO());
        when(orderService.getOrders(2, "testToken123")).thenReturn(list);

        ResponseEntity<?> response = orderController.getOrdersList(2, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
        verify(orderService).getOrders(2, "testToken123");
    }

    @Test
    void getOrdersList_error_returns404() {
        when(orderService.getOrders(anyInt(), anyString())).thenThrow(new RuntimeException("fail"));
        ResponseEntity<?> response = orderController.getOrdersList(7, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void orderConfirmation_callsService() {
        OrderStatusRequestDTO dto = new OrderStatusRequestDTO();
        orderController.orderConfirmation(dto);
        verify(orderService).orderConfirm(dto);
    }
}







//package com.orderservice.sprint4.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.orderservice.sprint4.dto.*;
//import com.orderservice.sprint4.service.OrderService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//class OrderControllerTest {
//    private MockMvc mockMvc;
//    @Mock
//    private OrderService orderService;
//    @InjectMocks
//    private OrderController orderController;
//    private ObjectMapper objectMapper = new ObjectMapper();
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
//    }
//
//    //Since the Return Type is changed for createOrder Method This Needed To be updated.
////    @Test
////    void createOrder_success() throws Exception {
////        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO();
////        when(orderService.createOrderTransaction(any())).thenReturn("INV123");
////        mockMvc.perform(post("/orders/create")
////                .contentType(MediaType.APPLICATION_JSON)
////                .content(objectMapper.writeValueAsString(dto)))
////                .andExpect(status().isOk())
////                .andExpect(content().string("Order created successfully with invoice number: INV123"));
////    }
//
//    @Test
//    void createOrder_success() throws Exception {
//        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO(); // build if needed
//
//        Map<String, String> orderItemIds = new HashMap<>();
//        orderItemIds.put("SKU123", "ORDITEM_ABC123");
//
//        OrderResponseDTO mockResponse = OrderResponseDTO.builder()
//                .orderItemIds(orderItemIds)
//                .status("success")
//                .build();
//
//        when(orderService.createOrderTransaction(any())).thenReturn(mockResponse);
//
//        mockMvc.perform(post("/orders/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.orderItemIds.SKU123").value("ORDITEM_ABC123"));
//    }
//
////    @Test
////    void createOrder_failure() throws Exception {
////        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO();
////        when(orderService.createOrderTransaction(any())).thenThrow(new RuntimeException("DB error"));
////        mockMvc.perform(post("/orders/create")
////                .contentType(MediaType.APPLICATION_JSON)
////                .content(objectMapper.writeValueAsString(dto)))
////                .andExpect(status().is5xxServerError())
////                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error Occurred: DB error")));
////    }
//
//    @Test
//    void createOrder_failure() throws Exception {
//        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO();
//
//        when(orderService.createOrderTransaction(any()))
//                .thenThrow(new RuntimeException("DB error"));
//
//        mockMvc.perform(post("/orders/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().is5xxServerError())
//                .andExpect(jsonPath("$.status").value("failure"))
//                .andExpect(jsonPath("$.orderItemIds").isMap());
//    }
//
//
//    @Test
//    void getOrderDetails_success() throws Exception {
//        OrderDetailsResponseDTO responseDTO = new OrderDetailsResponseDTO();
//        when(orderService.getOrderDetails(1)).thenReturn(responseDTO);
//        mockMvc.perform(get("/orders/1"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void getOrderDetails_notFound() throws Exception {
//        when(orderService.getOrderDetails(1)).thenThrow(new RuntimeException("Not found"));
//        mockMvc.perform(get("/orders/1"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void getOrdersList_success() throws Exception {
//        List<OrderSummaryDTO> mockOrders = new ArrayList<>();
//        mockOrders.add(new OrderSummaryDTO()); // Add mock data if needed
//
//        when(orderService.getOrders(7)).thenReturn(mockOrders);
//
//        mockMvc.perform(get("/orders/list/7"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void getOrdersList_failure() throws Exception {
//        when(orderService.getOrders(7)).thenThrow(new RuntimeException("Service error"));
//
//        mockMvc.perform(get("/orders/list/7"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void orderConfirmation_success() throws Exception {
//        OrderStatusRequestDTO dto = new OrderStatusRequestDTO(); // Populate fields if needed
//
//        doNothing().when(orderService).orderConfirm(any());
//
//        mockMvc.perform(patch("/orders/order/confirm")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isOk());
//    }
//
//
//}
//
