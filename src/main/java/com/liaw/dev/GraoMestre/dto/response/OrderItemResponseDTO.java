package com.liaw.dev.GraoMestre.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName; // Para facilitar a visualização
    private Integer quantity;
    private BigDecimal priceAtTime;
    private BigDecimal subtotal; // Adicionado para facilitar a visualização
}