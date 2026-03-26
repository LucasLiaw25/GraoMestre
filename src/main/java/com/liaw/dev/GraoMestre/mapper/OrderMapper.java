package com.liaw.dev.GraoMestre.mapper;

import com.liaw.dev.GraoMestre.dto.request.OrderItemRequestDTO;
import com.liaw.dev.GraoMestre.dto.request.OrderRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.OrderItemResponseDTO;
import com.liaw.dev.GraoMestre.dto.response.OrderResponseDTO;
import com.liaw.dev.GraoMestre.dto.response.PaymentResponseDTO;
import com.liaw.dev.GraoMestre.entity.Order;
import com.liaw.dev.GraoMestre.entity.OrderItem;
import com.liaw.dev.GraoMestre.entity.Payment;
import com.liaw.dev.GraoMestre.entity.Product;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {


    public static OrderItemResponseDTO toOrderItemResponseDTO(OrderItem orderItem) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtTime(orderItem.getPriceAtTime());
        dto.setSubtotal(orderItem.getPriceAtTime().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return dto;
    }

    public static PaymentResponseDTO toPaymentResponseDTO(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentUrl(payment.getPaymentUrl());
        dto.setTxId(payment.getTxId());
        dto.setTotalPrice(payment.getTotalPrice());
        return dto;
    }

    public static OrderResponseDTO toOrderResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserEmail(order.getUser().getName());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalPrice(order.getTotalPrice());

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            dto.setItems(order.getOrderItems().stream()
                    .map(OrderMapper::toOrderItemResponseDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(Collections.emptyList());
        }

        dto.setPayment(toPaymentResponseDTO(order.getPayment()));

        return dto;
    }

    public static List<OrderResponseDTO> toOrderResponseDTOList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(OrderMapper::toOrderResponseDTO)
                .collect(Collectors.toList());
    }
}