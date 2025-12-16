package com.prpo.chat.message.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PresenceClient {

    private final RestTemplate restTemplate;

    @Value("${presence.service.base-url}")
    private String baseUrl;

    public void setUserOnline(String userId) {
        try {
            String url = baseUrl + "/" + userId + "/online";
            restTemplate.put(url, null);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to set user status to online", e);
        }
    }
}