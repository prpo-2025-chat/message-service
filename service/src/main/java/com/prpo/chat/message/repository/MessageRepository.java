package com.prpo.chat.message.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.prpo.chat.message.entity.Message;

public interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByChannelIdOrderByDateSentDesc(
        String channelId, 
        Pageable pageable
    );

    List<Message> findByChannelIdInAndReadByNotContaining(
        List<String> channelIds,
        String userId
    );
}
