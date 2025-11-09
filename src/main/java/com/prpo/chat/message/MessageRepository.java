package com.prpo.chat.message;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {
  List<Message> findBysenderId(String receiverId);
  List<Message> findByChannelId(String channelId);
  Page<Message> findAll(Pageable pageable);
}
