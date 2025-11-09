package com.prpo.chat.message;

import java.util.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("messages")
public class Message {
  @Id private String id;
  private String channelId;
  private String senderId;
  private String content;
  @CreatedDate private Date dateSent;

  public Message() {}
  public Message(String channelId, String senderId, String content) {
    this.channelId = channelId;
    this.senderId = senderId;
    this.content = content;
  }

  public String getId() { return id; }
  public String getChannelId() { return channelId; }
  public String getSenderId() { return senderId; }
  public String getContent() { return content; }
  public Date getDateSent() { return dateSent; }

  public void setId(String id) { this.id = id; }
  public void setChannelId(String v) { this.channelId = v; }
  public void setSenderId(String v) { this.senderId = v; }
  public void setContent(String v) { this.content = v; }
  public void setDateSent(Date v) { this.dateSent = v; }
}

