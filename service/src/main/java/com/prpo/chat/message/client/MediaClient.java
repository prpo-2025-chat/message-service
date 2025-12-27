package com.prpo.chat.message.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.prpo.chat.message.entity.MediaAttachment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MediaClient {

    private final RestTemplate restTemplate;

    @Value("${media.service.base-url}")
    private String baseUrl;

    public MediaAttachment upload(byte[] content, String filename, String contentType, String uploaderId, String mediaType) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return filename;
                }
            }).header(HttpHeaders.CONTENT_TYPE, contentType);
            builder.part("uploaderId", uploaderId);
            builder.part("mediaType", mediaType);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<?> request = new HttpEntity<>(builder.build(), headers);
            String url = baseUrl + "/upload";

            return restTemplate.postForObject(url, request, MediaAttachment.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to upload media", e);
        }
    }
}
