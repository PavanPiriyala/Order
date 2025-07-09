package com.orderservice.sprint4.dto;

import com.orderservice.sprint4.dao.ShipmentItemDAO;
import com.orderservice.sprint4.model.enmus.OrderStatus;
import com.orderservice.sprint4.model.enmus.PaymentMode;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDetailsResponseDTO {
    private Integer orderId;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private PaymentMode paymentMode;
    private List<ShipmentItemDAO> items;
    // Lombok will generate all getters/setters/constructors/builders
    public Integer getOrderId() {
        return orderId;
    }
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
    public PaymentMode getPaymentMode() {
        return paymentMode;
    }
    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }
    public List<ShipmentItemDAO> getItems() {
        return items;
    }
    public void setItems(List<ShipmentItemDAO> items) {
        this.items = items;
    }

}
