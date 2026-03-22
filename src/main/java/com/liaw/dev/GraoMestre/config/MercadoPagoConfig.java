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

    @Value("${mercadopago.base.url:https://api.mercadopago.com}")
    private String baseUrl;

    private String webhookUrl;

    private String pendingUrl;

    private String failureUrl;

    private String successUrl;

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
