package com.liaw.dev.GraoMestre.dto.response;

import com.liaw.dev.GraoMestre.enums.PaymentMethod;
import com.liaw.dev.GraoMestre.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentResponseDTO {
    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String txId;
    private BigDecimal totalPrice;
}