package com.liaw.dev.GraoMestre.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
public class MercadoPagoConfig {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.urls.webhook}")
    private String webhookUrl;

    @Value("${mercadopago.urls.pending}")
    private String pendingUrl;

    @Value("${mercadopago.urls.failure}")
    private String failureUrl;

    @Value("${mercadopago.urls.success}")
    private String successUrl;

    @Value("${mercadopago.base.url:https://api.mercadopago.com}")
    private String baseUrl;
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
