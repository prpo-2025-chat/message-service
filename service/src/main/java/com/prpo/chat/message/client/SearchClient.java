package com.prpo.chat.message.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.prpo.chat.message.client.dto.IndexMessageRequestDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchClient {

    private final RestTemplate restTemplate;

    @Value("${search.service.base-url}")
    private String baseUrl;

    public void indexMessage(IndexMessageRequestDto indexMessage) {
        try {
            String url = baseUrl + "/index/message";
            restTemplate.postForLocation(url, indexMessage);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to send message to search service", e);
        }
    }
}