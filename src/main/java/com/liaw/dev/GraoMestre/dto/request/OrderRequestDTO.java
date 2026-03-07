package com.liaw.dev.GraoMestre.dto.request;


import com.liaw.dev.GraoMestre.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    @NotNull(message = "Método de pagamento é obrigatório")
    private PaymentMethod paymentMethod;

    @Valid
    @Size(min = 1, message = "O pedido deve conter pelo menos um item")
    private List<OrderItemRequestDTO> items;
}