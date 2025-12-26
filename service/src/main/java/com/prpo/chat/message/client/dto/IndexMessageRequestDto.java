package com.prpo.chat.message.client.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexMessageRequestDto {
    private String messageId;
    private String senderId;
    private String channelId;
    private String text;
    private Date dateSent;
}
