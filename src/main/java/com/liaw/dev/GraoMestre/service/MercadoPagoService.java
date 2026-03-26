package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.config.MercadoPagoConfig;
import com.liaw.dev.GraoMestre.dto.response.MercadoPagoWebhookNotificationDTO;
import com.liaw.dev.GraoMestre.entity.Order;
import com.liaw.dev.GraoMestre.entity.OrderItem;
import com.liaw.dev.GraoMestre.entity.Payment;
import com.liaw.dev.GraoMestre.enums.OrderStatus;
import com.liaw.dev.GraoMestre.enums.PaymentStatus;
import com.liaw.dev.GraoMestre.repository.OrderRepository;
import com.liaw.dev.GraoMestre.repository.PaymentRepository;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentItemRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MercadoPagoService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoService.class);
    private final com.liaw.dev.GraoMestre.config.MercadoPagoConfig appConfig;
    private final PaymentClient paymentClient;
    private final PreferenceClient preferenceClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    public MercadoPagoService(MercadoPagoConfig appConfig) {
        this.appConfig = appConfig;
        com.mercadopago.MercadoPagoConfig.setAccessToken(appConfig.getAccessToken());
        this.paymentClient = new PaymentClient();
        this.preferenceClient = new PreferenceClient();
    }

    @Transactional
    public com.liaw.dev.GraoMestre.entity.Payment createMercadoPagoPayment(Order order, com.liaw.dev.GraoMestre.entity.Payment paymentEntity) throws MPException, MPApiException {
        if (order == null || paymentEntity == null) {
            throw new IllegalArgumentException("Order e Payment não podem ser nulos.");
        }

        if (paymentEntity.getMpPaymentId() != null || paymentEntity.getMpPreferenceId() != null) {
            return paymentEntity;
        }

        switch (paymentEntity.getPaymentMethod()) {
            case PIX:
            case CREDIT_CARD:
            case DEBIT_CARD:
                return createCheckoutProPreference(order, paymentEntity);
            default:
                throw new IllegalArgumentException("Método de pagamento não suportado pelo Mercado Pago: " + paymentEntity.getPaymentMethod());
        }
    }

    private Payment createPixPayment(Order order,Payment paymentEntity) throws MPException, MPApiException{
        List<PaymentItemRequest> items = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()){
            items.add(
                    PaymentItemRequest.builder()
                            .id(String.valueOf(orderItem.getProduct().getId()))
                            .title(orderItem.getProduct().getName())
                            .description(orderItem.getProduct().getDescription())
                            .quantity(orderItem.getQuantity())
                            .unitPrice(orderItem.getPriceAtTime())
                            .build()
            );
        }

        IdentificationRequest identification = IdentificationRequest.builder()
                .type("CPF")
                .number(order.getUser().getCpf())
                .build();

        PaymentPayerRequest payer = PaymentPayerRequest.builder()
                .email(order.getUser().getEmail())
                .firstName(order.getUser().getName())
                .identification(identification)
                .build();

        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                .transactionAmount(order.getTotalPrice())
                .description("Pagamento para o pedido #" + order.getId())
                .paymentMethodId("pix")
                .payer(payer)
                .externalReference(String.valueOf(order.getId()))
                .notificationUrl(appConfig.getWebhookUrl())
                .dateOfExpiration(OffsetDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.SECONDS))
                .build();

        com.mercadopago.resources.payment.Payment mpPayment = paymentClient.create(paymentCreateRequest);
        paymentEntity.setMpPaymentId(String.valueOf(mpPayment.getId()));
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setTxId(mpPayment.getTransactionDetails().getExternalResourceUrl());
        paymentEntity.setPaymentUrl(mpPayment.getPointOfInteraction().getTransactionData().getTicketUrl());
        paymentEntity.setQrCodeBase64(mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64());
        paymentEntity.setQrCodeText(mpPayment.getPointOfInteraction().getTransactionData().getQrCode());
        paymentEntity.setDateOfExpiration(mpPayment.getDateOfExpiration().toLocalDateTime());

        return paymentEntity;

    }

    private Payment createCheckoutProPreference(Order order, Payment paymentEntity) throws MPException, MPApiException{
        List<PreferenceItemRequest> items = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()){
            items.add(
                    PreferenceItemRequest.builder()
                            .id(String.valueOf(orderItem.getProduct().getId()))
                            .title(orderItem.getProduct().getName())
                            .description(orderItem.getProduct().getDescription())
                            .unitPrice(orderItem.getPriceAtTime())
                            .quantity(orderItem.getQuantity())
                            .build()
            );
        }

        IdentificationRequest identification = IdentificationRequest.builder()
                .type("CPF")
                .number(order.getUser().getCpf())
                .build();

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(order.getUser().getEmail())
                .identification(identification)
                .name(order.getUser().getName())
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .failure(appConfig.getFailureUrl())
                .success(appConfig.getSuccessUrl())
                .pending(appConfig.getPendingUrl())
                .build();

        OffsetDateTime expirationDate = OffsetDateTime.now(ZoneOffset.of("-03:00"))
                .plusMinutes(30).truncatedTo(ChronoUnit.SECONDS);

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(String.valueOf(order.getId()))
                .notificationUrl(appConfig.getWebhookUrl())
                .dateOfExpiration(expirationDate)
                .statementDescriptor("GRAOMESTRE")
                .build();

        Preference mpPreference= preferenceClient.create(preferenceRequest);
        paymentEntity.setMpPreferenceId(mpPreference.getId());
        paymentEntity.setPaymentUrl(mpPreference.getInitPoint());
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setDateCreated(LocalDateTime.now());

        return paymentEntity;
    }

    @Transactional
    public void processNotification(MercadoPagoWebhookNotificationDTO notification) throws MPException, MPApiException {
        String resourceId = notification.getData().getId();
        String resourceType = notification.getType();

        if ("payment".equals(resourceType)) {
            log.info("Processando notificação de pagamento para ID: {}", resourceId);
            com.mercadopago.resources.payment.Payment mpPayment = paymentClient.get(Long.valueOf(resourceId));

            Optional<Payment> optionalPayment = paymentRepository.findByMpPaymentId(String.valueOf(mpPayment.getId()));
            if (optionalPayment.isEmpty()) {
                log.warn("Pagamento local não encontrado pelo mpPaymentId {}. Tentando buscar pelo external_reference (Order ID).", mpPayment.getId());
                String externalReference = mpPayment.getExternalReference();
                if (externalReference != null) {
                    Optional<Order> optionalOrder = orderRepository.findById(Long.valueOf(externalReference));
                    if (optionalOrder.isPresent()) {
                        optionalPayment = Optional.ofNullable(optionalOrder.get().getPayment());
                    }
                }
            }


            if (optionalPayment.isPresent()) {
                Payment localPayment = optionalPayment.get();
                Order order = localPayment.getOrder();

                PaymentStatus newPaymentStatus = mapMercadoPagoStatusToPaymentStatus(mpPayment.getStatus());
                OrderStatus newOrderStatus = mapMercadoPagoStatusToOrderStatus(mpPayment.getStatus());

                if (localPayment.getPaymentStatus() != newPaymentStatus || order.getOrderStatus() != newOrderStatus) {
                    localPayment.setPaymentStatus(newPaymentStatus);
                    order.setOrderStatus(newOrderStatus);

                    if (newPaymentStatus == PaymentStatus.PAID && mpPayment.getDateApproved() != null) {
                        localPayment.setDateApproved(mpPayment.getDateApproved().toLocalDateTime());
                    }

                    paymentRepository.save(localPayment);
                    orderRepository.save(order);
                    log.info("Status do pagamento e pedido atualizados para o pedido ID {}. Novo status MP: {}, Novo status local: {}",
                            order.getId(), mpPayment.getStatus(), newPaymentStatus);
                } else {
                    log.info("Status do pagamento para o pedido ID {} já está atualizado. Status: {}", order.getId(), newPaymentStatus);
                }
            } else {
                log.warn("Pagamento local não encontrado para o MP Payment ID: {}", resourceId);
            }
        } else if ("merchant_order".equals(resourceType)) {

            log.info("Notificação de Merchant Order recebida, mas não processada: {}", resourceId);
        } else {
            log.warn("Tipo de notificação do Mercado Pago desconhecido: {}", resourceType);
        }
    }


    private PaymentStatus mapMercadoPagoStatusToPaymentStatus(String mpStatus) {
        switch (mpStatus) {
            case "approved":
                return PaymentStatus.PAID;
            case "pending":
            case "in_process":
                return PaymentStatus.PENDING;
            case "rejected":
            case "cancelled":
            case "refunded":
            case "charged_back":
                return PaymentStatus.FAILED;
            default:
                return PaymentStatus.PENDING;
        }
    }

    private OrderStatus mapMercadoPagoStatusToOrderStatus(String mpStatus) {
        switch (mpStatus) {
            case "approved":
                return OrderStatus.PAID;
            case "pending":
            case "in_process":
                return OrderStatus.PENDING;
            case "rejected":
            case "cancelled":
            case "refunded":
            case "charged_back":
                return OrderStatus.CANCELED;
            default:
                return OrderStatus.PENDING;
        }
    }

}
