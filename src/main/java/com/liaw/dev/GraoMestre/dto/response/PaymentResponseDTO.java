package com.liaw.dev.GraoMestre.dto.response;

import com.liaw.dev.GraoMestre.entity.Order;
import com.liaw.dev.GraoMestre.enums.PaymentMethod;
import com.liaw.dev.GraoMestre.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {
    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String txId;
    private BigDecimal totalPrice;
    private String qrCodeBase64;
    private String qrCodeText;
    private String paymentUrl;
    private LocalDateTime dateOfExpiration;
    private LocalDateTime dateApproved;
}

