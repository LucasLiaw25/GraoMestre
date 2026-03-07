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
    private UserResponseDTO user;
    private List<OrderItemResponseDTO> orderItems;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private PaymentResponseDTO payment;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
}