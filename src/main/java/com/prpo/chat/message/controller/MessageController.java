package com.prpo.chat.message.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

import com.prpo.chat.message.MessageRequest;
import com.prpo.chat.message.MessageResponse;


@RestController
@RequestMapping("/message")
public class MessageController {
    
    @GetMapping
    public String getAllMessages() {
        return "This are all available messages";
    }

    @GetMapping("/{id}")
    public String getMessageById(@PathVariable String id) {
        return "This is the message for id: " + id;
    }

    @PostMapping
    public MessageResponse postMessage(@RequestBody MessageRequest body) {
        String id = UUID.randomUUID().toString();
        return new MessageResponse(
            id,
            body.senderId(),
            body.recieverId(),
            body.message(),
            "Ok -> 200"
        );
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteMessage(@PathVariable String id) {
        return Map.of("Status", "deleted", "Content", "Deleted id: " + id);
    }
}
