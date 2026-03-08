package com.liaw.dev.GraoMestre.mapper;

import com.liaw.dev.GraoMestre.dto.request.OrderItemRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.OrderItemResponseDTO;
import com.liaw.dev.GraoMestre.entity.OrderItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemRequestDTO dto) {
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(dto.getQuantity());
        return orderItem;
    }

    public static OrderItemResponseDTO toResponseDTO(OrderItem orderItem) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtTime(orderItem.getPriceAtTime());
        if (orderItem.getProduct() != null) {
            dto.setProductId(orderItem.getProduct().getId());
            dto.setProductName(orderItem.getProduct().getName());
        }
        return dto;
    }

    public static List<OrderItemResponseDTO> toResponseDTOList(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return Collections.emptyList();
        }
        return orderItems.stream()
                .map(OrderItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(OrderItemRequestDTO dto, OrderItem orderItem) {
        orderItem.setQuantity(dto.getQuantity());
    }
}