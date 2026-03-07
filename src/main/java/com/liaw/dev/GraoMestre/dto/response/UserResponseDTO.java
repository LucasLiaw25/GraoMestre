package com.liaw.dev.GraoMestre.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private LocalDateTime registerDate;
    private Boolean active;
    private List<ScopeResponseDTO> scopes;
}