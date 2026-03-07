package com.liaw.dev.GraoMestre.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotBlank(message = "Rua é obrigatória")
    private String street;

    @NotBlank(message = "Número é obrigatório")
    private String number;

    private String complement;

    @NotBlank(message = "Estado é obrigatório")
    private String state;

    @NotBlank(message = "Cidade é obrigatória")
    private String city;

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-\\d{3}|\\d{8}", message = "CEP inválido. Use o formato XXXXX-XXX ou XXXXXXXX")
    private String cep;

    private Boolean isDefault;

    @NotNull(message = "ID do usuário é obrigatório")
    private Long userId;
}