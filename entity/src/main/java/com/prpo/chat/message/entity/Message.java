package com.prpo.chat.message.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("messages")
@CompoundIndexes({
  @CompoundIndex(name = "channel_date_idx", def = "{'channelId': 1, 'dateSent': -1}"),
  @CompoundIndex(name = "channel_id_idx", def = "{'channelId': 1, '_id': 1}")
})
public class Message {

  @Id 
  private String id;

  @Indexed
  private String channelId;

  private String senderId;
  
  private String content;

  private MessageStatus status;

  private Set<String> readBy;

  @CreatedDate 
  private Date dateSent;

  public Message() {}
  public Message(String channelId, String senderId, String content) {
    this.channelId = channelId;
    this.senderId = senderId;
    this.content = content;
    this.status = MessageStatus.SENT;
    this.readBy = new HashSet<>(Set.of(senderId));
  }
  public Message(
    String id, 
    String channelId, 
    String senderId, 
    String content, 
    MessageStatus status, 
    Set<String> readBy,
    Date dateSent
  ) {
    this.id = id;
    this.channelId = channelId;
    this.senderId = senderId;
    this.content = content;
    this.status = status;
    this.readBy = readBy;
    this.dateSent = dateSent;
  }

  public String getId() { return id; }
  public String getChannelId() { return channelId; }
  public String getSenderId() { return senderId; }
  public String getContent() { return content; }
  public Date getDateSent() { return dateSent; }
  public MessageStatus getStatus() { return status; }
  public Set<String> getReadBy() { return readBy; }

  public void setId(String id) { this.id = id; }
  public void setChannelId(String v) { this.channelId = v; }
  public void setSenderId(String v) { this.senderId = v; }
  public void setContent(String v) { this.content = v; }
  public void setDateSent(Date v) { this.dateSent = v; }
  public void setStatus(MessageStatus v) { this.status = v; }
  public void setReadBy(Set<String> v) { this.readBy = v; }
}

