package com.prpo.chat.message.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prpo.chat.message.dto.Message;


@RestController
@RequestMapping("/message")
public class MessageController {
    
    @GetMapping
    public List<Message> getAllMessages() {
        return null;
    }

    @GetMapping("/{id}")
    public String getMessageById(@PathVariable String id) {
        return "This is the message for id: " + id;
    }

    @PostMapping
    public Message sendMessage(@RequestBody Message body) {
        String id = UUID.randomUUID().toString();
        Message message = body;
        message.setId(id);
        message.setDateSent(new Date());
        return message;

    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteMessage(@PathVariable String id) {
        return Map.of("Status", "deleted", "Content", "Deleted id: " + id);
    }
}
