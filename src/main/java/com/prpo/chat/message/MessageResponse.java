package com.prpo.chat.message;

public record MessageResponse(
        String id,
        String senderId,
        String receiverId,
        String message,
        String status
) {}
