package com.prpo.chat.message.dto;

import java.util.Date;
import java.util.Set;

import com.prpo.chat.message.entity.MessageStatus;

import jakarta.validation.constraints.NotBlank;

public class MessageDto {

    private String id;
    
    @NotBlank
    private String channelId;
    
    @NotBlank
    private String senderId;
    
    @NotBlank
    private String content;
    
    private MessageStatus status;
    
    private Set<String> readBy;
    
    private Date dateSent;

    public MessageDto() {
    }

    public MessageDto(String id, String channelId, String senderId, String content,
                      MessageStatus status, Set<String> readBy, Date dateSent) {
        this.id = id;
        this.channelId = channelId;
        this.senderId = senderId;
        this.content = content;
        this.status = status;
        this.readBy = readBy;
        this.dateSent = dateSent;
    }

    public String getId() {
        return id;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public Set<String> getReadBy() {
        return readBy;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public void setReadBy(Set<String> readBy) {
        this.readBy = readBy;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }
}
