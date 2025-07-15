package com.orderservice.sprint4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.sprint4.dto.*;
import com.orderservice.sprint4.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {
    private MockMvc mockMvc;
    @Mock
    private OrderService orderService;
    @InjectMocks
    private OrderController orderController;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    //Since the Return Type is changed for createOrder Method This Needed To be updated.
//    @Test
//    void createOrder_success() throws Exception {
//        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO();
//        when(orderService.createOrderTransaction(any())).thenReturn("INV123");
//        mockMvc.perform(post("/orders/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Order created successfully with invoice number: INV123"));
//    }

    @Test
    void createOrder_success() throws Exception {
        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO(); // build if needed

        Map<String, String> orderItemIds = new HashMap<>();
        orderItemIds.put("SKU123", "ORDITEM_ABC123");

        OrderResponseDTO mockResponse = OrderResponseDTO.builder()
                .orderItemIds(orderItemIds)
                .status("success")
                .build();

        when(orderService.createOrderTransaction(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.orderItemIds.SKU123").value("ORDITEM_ABC123"));
    }

//    @Test
//    void createOrder_failure() throws Exception {
//        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO();
//        when(orderService.createOrderTransaction(any())).thenThrow(new RuntimeException("DB error"));
//        mockMvc.perform(post("/orders/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().is5xxServerError())
//                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error Occurred: DB error")));
//    }

    @Test
    void createOrder_failure() throws Exception {
        OrderDetailsRequestDTO dto = new OrderDetailsRequestDTO();

        when(orderService.createOrderTransaction(any()))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value("failure"))
                .andExpect(jsonPath("$.orderItemIds").isMap());
    }


    @Test
    void getOrderDetails_success() throws Exception {
        OrderDetailsResponseDTO responseDTO = new OrderDetailsResponseDTO();
        when(orderService.getOrderDetails(1)).thenReturn(responseDTO);
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderDetails_notFound() throws Exception {
        when(orderService.getOrderDetails(1)).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersList_success() throws Exception {
        List<OrderSummaryDTO> mockOrders = new ArrayList<>();
        mockOrders.add(new OrderSummaryDTO()); // Add mock data if needed

        when(orderService.getOrders(7)).thenReturn(mockOrders);

        mockMvc.perform(get("/orders/list/7"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrdersList_failure() throws Exception {
        when(orderService.getOrders(7)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/orders/list/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void orderConfirmation_success() throws Exception {
        OrderStatusRequestDTO dto = new OrderStatusRequestDTO(); // Populate fields if needed

        doNothing().when(orderService).orderConfirm(any());

        mockMvc.perform(patch("/orders/order/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }


}

