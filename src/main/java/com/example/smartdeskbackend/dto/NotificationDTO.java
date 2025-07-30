package com.example.smartdeskbackend.dto;

/**
 * WebSocket bildirimleri için DTO sınıfı
 */
public class NotificationDTO {
    private String message;
    private String type;
    private String url;

    // Boş constructor (JSON deserializasyon için gerekli)
    public NotificationDTO() {}

    // Parametreli constructor (kodda kullanılan)
    public NotificationDTO(String message, String type, String url) {
        this.message = message;
        this.type = type;
        this.url = url;
    }

    // Getter ve Setter metodları
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "NotificationDTO{" +
                "message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}