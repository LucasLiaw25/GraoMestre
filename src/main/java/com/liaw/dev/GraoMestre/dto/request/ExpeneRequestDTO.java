package com.liaw.dev.GraoMestre.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpeneRequestDTO {
    private String name;
    private BigDecimal price;
}
