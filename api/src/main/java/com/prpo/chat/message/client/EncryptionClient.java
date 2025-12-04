package com.prpo.chat.message.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EncryptionClient {

    private final RestTemplate restTemplate;

    @Value("${encryption.service.base-url}")
    private String baseUrl;

    /**
     * Encrypt a single string using:
     * POST /encryption with String body → String response
     */
    public String encrypt(String plainText) {
        try {
            String url = baseUrl;
            return restTemplate.postForObject(url, plainText, String.class);
        } catch (RestClientException e) {
            // wrap in RuntimeException so callers don't have to handle checked exceptions
            throw new RuntimeException("Error calling encryption service (encrypt)", e);
        }
    }

    /**
     * Decrypt a single string using:
     * POST /encryption/decryption with String body → String response
     */
    public String decrypt(String cipherText) {
        try {
            String url = baseUrl + "/decryption";
            return restTemplate.postForObject(url, cipherText, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling encryption service (decrypt)", e);
        }
    }

    /**
     * Decrypt multiple ciphertexts using:
     * POST /encryption/decryption/batch with List<String> body → List<String> response
     */
    public List<String> decryptBatch(List<String> ciphertexts) {
        try {
            String url = baseUrl + "/decryption/batch";

            HttpEntity<List<String>> requestEntity = new HttpEntity<>(ciphertexts);

            return restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<String>>() {}
            ).getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling encryption service (decryptBatch)", e);
        }
    }
}