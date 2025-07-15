package com.orderservice.sprint4.service;

import com.orderservice.sprint4.dto.*;
import com.orderservice.sprint4.model.OrderItem;

import java.util.List;

public interface OrderService {

    void orderConfirm(OrderStatusRequestDTO dto);
    //Changed
    OrderResponseDTO createOrderTransaction(OrderDetailsRequestDTO dto,String token);
    OrderDetailsResponseDTO getOrderDetails(Integer orderId,String token);
    List<OrderSummaryDTO> getOrders(Integer months,String token);
}
