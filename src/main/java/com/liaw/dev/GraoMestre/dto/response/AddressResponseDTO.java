package com.liaw.dev.GraoMestre.dto.response;
import lombok.Data;

@Data
public class AddressResponseDTO {
    private Long id;
    private String street;
    private String number;
    private String complement;
    private String state;
    private String city;
    private String cep;
    private Boolean isDefault;
    private Long userId;
}