package com.liaw.dev.GraoMestre.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoWebhookNotificationDTO {
    private Long id;
    private String type;
    private String action;
    private String api_version;
    private String live_mode;
    private String user_id;
    private String application_id;
    private String date_created;
    private String version;

    @JsonProperty("data")
    private NotificationData data;

    @Data
    public static class NotificationData {
        private String id;
    }
}
