package com.liaw.dev.GraoMestre.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScopeRequestDTO {
    @NotBlank(message = "Nome do escopo é obrigatório")
    private String name;
    private String description;
}