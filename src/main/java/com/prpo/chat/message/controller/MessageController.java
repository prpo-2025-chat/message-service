package com.prpo.chat.message.controller;

import org.springframework.web.bind.annotation.*;

import com.prpo.chat.message.Message;
import com.prpo.chat.message.MessageRepository;

import java.util.*;


@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageRepository repo;

    public MessageController(MessageRepository repo) { this.repo = repo; }
    
    @GetMapping
    public List<Message> getAllMessages() {
        return this.repo.findAll();
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
        this.repo.save(message);
        return message;

    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteMessage(@PathVariable String id) {
        return Map.of("Status", "deleted", "Content", "Deleted id: " + id);
    }
}
