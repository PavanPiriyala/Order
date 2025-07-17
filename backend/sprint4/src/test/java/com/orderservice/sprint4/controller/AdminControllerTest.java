package com.orderservice.sprint4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.sprint4.dto.OrderItemStatusRequestDTO;
import com.orderservice.sprint4.dto.ShipmentItemListDTO;
import com.orderservice.sprint4.model.enmus.ShipmentItemStatus;
import com.orderservice.sprint4.service.AdminService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void orderShipped_callsService() throws Exception {
        OrderItemStatusRequestDTO dto = new OrderItemStatusRequestDTO();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch("/admin/order/shipped")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(adminService).orderShipped(ArgumentMatchers.any(OrderItemStatusRequestDTO.class));
    }

    @Test
    void orderDelivered_callsService() throws Exception {
        OrderItemStatusRequestDTO dto = new OrderItemStatusRequestDTO();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch("/admin/order/delivered")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(adminService).orderDelivered(ArgumentMatchers.any(OrderItemStatusRequestDTO.class));
    }

    @Test
    void orderCancellation_callsService() throws Exception {
        OrderItemStatusRequestDTO dto = new OrderItemStatusRequestDTO();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch("/admin/order/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(adminService).orderCanelled(ArgumentMatchers.any(OrderItemStatusRequestDTO.class));
    }

    @Test
    void getOrdersList_returnsContent() throws Exception {
        ShipmentItemListDTO shipmentItem = new ShipmentItemListDTO();
        // Set necessary fields on shipmentItem if needed

        when(adminService.getShipmentByStatus(ShipmentItemStatus.Pending))
                .thenReturn(List.of(shipmentItem));

        mockMvc.perform(get("/admin/orders/get")
                        .param("itemStatus", "Pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getOrdersList_returnsNoContentWhenEmpty() throws Exception {
        when(adminService.getShipmentByStatus(ShipmentItemStatus.Delivered))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/orders/get")
                        .param("itemStatus", "Delivered"))
                .andExpect(status().isNoContent());
    }
}
