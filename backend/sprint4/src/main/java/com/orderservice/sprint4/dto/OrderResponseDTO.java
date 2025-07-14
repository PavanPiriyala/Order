package com.orderservice.sprint4.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class OrderResponseDTO {
    private Map<String, String> orderItemIds;
    private String status;
}
