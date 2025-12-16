package com.prpo.chat.message.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceivedNotificationRequest {
    private String messageId;
    private String senderId;
    private String channelId;
    private String text;
}
