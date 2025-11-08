package com.prpo.chat.message;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(
        @NotBlank String senderId,
        @NotBlank String recieverId,
        @NotBlank String message
) {}