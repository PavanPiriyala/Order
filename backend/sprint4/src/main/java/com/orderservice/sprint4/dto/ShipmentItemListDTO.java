package com.orderservice.sprint4.dto;

import com.orderservice.sprint4.model.enmus.ShipmentItemStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class ShipmentItemListDTO {
    private Integer orderId;
    private Integer orderItemId;
    private ShipmentItemStatus itemStatus;
    private LocalDateTime shipmentDate;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public ShipmentItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(ShipmentItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }

    public LocalDateTime getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(LocalDateTime shipmentDate) {
        this.shipmentDate = shipmentDate;
    }
}
