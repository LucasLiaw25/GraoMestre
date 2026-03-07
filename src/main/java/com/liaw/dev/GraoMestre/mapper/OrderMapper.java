package com.liaw.dev.GraoMestre.mapper;

import com.liaw.dev.GraoMestre.dto.request.OrderRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.OrderResponseDTO;
import com.liaw.dev.GraoMestre.entity.Order;
import com.liaw.dev.GraoMestre.entity.OrderItem;
import com.liaw.dev.GraoMestre.entity.User; // Importar User para o stub
import com.liaw.dev.GraoMestre.enums.OrderStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static Order toEntity(OrderRequestDTO dto) {
        Order order = new Order();
        order.setPaymentMethod(dto.getPaymentMethod());
        return order;
    }

    public static OrderResponseDTO toResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderDate(order.getOrderDate());

        if (order.getUser() != null) {
            dto.setUser(UserMapper.toResponseDTO(order.getUser()));
        }
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(OrderItemMapper::toResponseDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setOrderItems(Collections.emptyList());
        }
        if (order.getPayment() != null) {
            dto.setPayment(PaymentMapper.toResponseDTO(order.getPayment()));
        }
        return dto;
    }

    public static List<OrderResponseDTO> toResponseDTOList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(OrderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(OrderRequestDTO dto, Order order) {
        order.setPaymentMethod(dto.getPaymentMethod());
    }
}