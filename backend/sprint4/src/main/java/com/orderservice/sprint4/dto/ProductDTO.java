package com.orderservice.sprint4.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {
    private Integer id;
    private String name;

}
