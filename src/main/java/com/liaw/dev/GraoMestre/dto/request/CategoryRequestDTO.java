package com.liaw.dev.GraoMestre.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    @NotBlank(message = "Nome da categoria é obrigatório")
    private String name;
    private String description;
}