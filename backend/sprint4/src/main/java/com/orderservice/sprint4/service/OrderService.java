package com.orderservice.sprint4.service;

import com.orderservice.sprint4.dto.OrderDetailsRequestDTO;
import com.orderservice.sprint4.dto.OrderDetailsResponseDTO;
import com.orderservice.sprint4.dto.OrderStatusRequestDTO;
import com.orderservice.sprint4.dto.OrderSummaryDTO;

import java.util.List;

public interface OrderService {

    void orderConfirm(OrderStatusRequestDTO dto);
    String createOrderTransaction(OrderDetailsRequestDTO dto);
    OrderDetailsResponseDTO getOrderDetails(Integer orderId);
    List<OrderSummaryDTO> getOrders(Integer months);
}
