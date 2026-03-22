package com.liaw.dev.GraoMestre.controller;

import com.liaw.dev.GraoMestre.dto.response.MercadoPagoWebhookNotificationDTO;
import com.liaw.dev.GraoMestre.service.MercadoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/mercadopago")
public class MercadoPagoWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookController.class);

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @PostMapping
    public ResponseEntity<Void> receiveWebhookNotification(@RequestBody MercadoPagoWebhookNotificationDTO notification) {
        log.info("Webhook do Mercado Pago recebido. Tipo: {}, Ação: {}, ID do Recurso: {}",
                notification.getType(), notification.getAction(), notification.getData().getId());

        try {
            mercadoPagoService.processNotification(notification);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao processar notificação do Mercado Pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
