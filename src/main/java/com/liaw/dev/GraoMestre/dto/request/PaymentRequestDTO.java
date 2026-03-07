package com.liaw.dev.GraoMestre.dto.request;

import com.liaw.dev.GraoMestre.enums.PaymentMethod;
import com.liaw.dev.GraoMestre.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    @NotNull(message = "Método de pagamento é obrigatório")
    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;
    private String txId;
    private BigDecimal totalPrice;
}