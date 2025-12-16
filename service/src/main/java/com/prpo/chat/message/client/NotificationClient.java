package com.prpo.chat.message.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.prpo.chat.message.client.dto.MessageReceivedNotificationRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${notification.service.base-url}")
    private String baseUrl;

    public void notifyMessageReceived(MessageReceivedNotificationRequest request) {
        try {
            String url = baseUrl + "/internal/notifications/message-received";
            restTemplate.postForLocation(url, request);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to notify notification-service", e);
        }
    }
}