package com.liaw.dev.GraoMestre.dto.response;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long id;
    private ProductResponseDTO product;
    private Integer quantity;
    private BigDecimal priceAtTime;
}