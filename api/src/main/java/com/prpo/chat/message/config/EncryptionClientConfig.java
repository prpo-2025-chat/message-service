package com.prpo.chat.message.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.mongodb.lang.NonNull;

@Configuration
public class EncryptionClientConfig {

    @Bean
    public WebClient encryptionWebClient(
            WebClient.Builder builder,
            @Value("${encryption-service.base-url}") @NonNull String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
