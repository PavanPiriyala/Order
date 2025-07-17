package com.orderservice.sprint4.controller;

import com.orderservice.sprint4.dto.ShipmentDetailsResponseDTO;
import com.orderservice.sprint4.service.InvoiceService;
import com.orderservice.sprint4.service.ShipmentService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentControllerTest {

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private ShipmentController shipmentController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getShipmentItemsByOrderId_returnsResponse() {
        int orderId = 10;
        ShipmentDetailsResponseDTO dto = new ShipmentDetailsResponseDTO();
        when(shipmentService.getShipmentItemsByOrderId(orderId)).thenReturn(dto);

        ResponseEntity<?> response = shipmentController.getShipmentItemsByOrderId(orderId);

        assertEquals(200, response.getStatusCodeValue());
        assertSame(dto, response.getBody());
        verify(shipmentService).getShipmentItemsByOrderId(orderId);
    }

    @Test
    void generateInvoicePdf_callsInvoiceService() throws IOException {
        int orderId = 100;
        HttpServletResponse response = mock(HttpServletResponse.class);

        // method returns void, just verify call
        shipmentController.generateInvoicePdf(orderId, response);

        verify(response).setContentType("application/pdf");
        verify(response).setHeader("content-Disposition", "attachment; filename=order_100.pdf");
        verify(invoiceService).generatepdf(orderId, response);
    }
}
