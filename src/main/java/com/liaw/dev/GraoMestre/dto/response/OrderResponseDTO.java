package com.liaw.dev.GraoMestre.dto.response;

import com.liaw.dev.GraoMestre.enums.OrderStatus;
import com.liaw.dev.GraoMestre.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private String userEmail; // Para facilitar a visualização
    private List<OrderItemResponseDTO> items;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private PaymentResponseDTO payment; // Detalhes do pagamento
    private LocalDateTime orderDate;
    private BigDecimal totalPrice;
}