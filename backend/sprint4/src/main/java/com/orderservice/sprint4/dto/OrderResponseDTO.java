package com.orderservice.sprint4.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;


public class OrderResponseDTO {
    private Map<String, String> orderItemIds;
    private String status;

    public Map<String, String> getOrderItemIds() {
        return orderItemIds;
    }

    public void setOrderItemIds(Map<String, String> orderItemIds) {
        this.orderItemIds = orderItemIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
