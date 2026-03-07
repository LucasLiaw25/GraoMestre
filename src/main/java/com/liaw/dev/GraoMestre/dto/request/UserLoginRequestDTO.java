package com.liaw.dev.GraoMestre.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequestDTO {
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String password;
}