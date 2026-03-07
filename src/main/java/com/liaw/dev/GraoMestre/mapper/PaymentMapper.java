package com.liaw.dev.GraoMestre.mapper;


import com.liaw.dev.GraoMestre.dto.request.PaymentRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.PaymentResponseDTO;
import com.liaw.dev.GraoMestre.entity.Payment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentMapper {

    public static Payment toEntity(PaymentRequestDTO dto) {
        Payment payment = new Payment();
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setTxId(dto.getTxId());
        payment.setTotalPrice(dto.getTotalPrice());
        return payment;
    }

    public static PaymentResponseDTO toResponseDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setTxId(payment.getTxId());
        dto.setTotalPrice(payment.getTotalPrice());
        if (payment.getOrder() != null) {
            dto.setOrderId(payment.getOrder().getId());
        }
        return dto;
    }

    public static List<PaymentResponseDTO> toResponseDTOList(List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            return Collections.emptyList();
        }
        return payments.stream()
                .map(PaymentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(PaymentRequestDTO dto, Payment payment) {
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setTxId(dto.getTxId());
        payment.setTotalPrice(dto.getTotalPrice());
    }
}