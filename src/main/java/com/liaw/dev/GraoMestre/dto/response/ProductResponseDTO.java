package com.liaw.dev.GraoMestre.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Integer storage;
    private String imageUrl;
    private LocalDateTime registerDate;
    private BigDecimal price;
    private Boolean active;
    private CategoryResponseDTO category;
}