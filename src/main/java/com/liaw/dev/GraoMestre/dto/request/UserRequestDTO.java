package com.liaw.dev.GraoMestre.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserRequestDTO {
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Telefone é obrigatório")
    private String phone;

    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String password;

    private Boolean active;

    private List<Long> scopeIds;
}