package com.prpo.chat.message.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prpo.chat.message.dto.MessageDto;
import com.prpo.chat.message.entity.Message;
import com.prpo.chat.message.service.MessageService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @GetMapping
    public Page<Message> getMessagesForChannel(
        @RequestParam("channelId") String channelId,
        @RequestParam("pageNo") int pageNo,
        @RequestParam("pageSize") int pageSize
    ) {
        return messageService.getConversation(channelId, pageNo, pageSize);
    }

    @GetMapping("/inbox")
    public List<Message> getInboxForUserId(@RequestParam("userId") String userId) {
        return messageService.getInbox(userId);
    }

    @GetMapping("/{id}")
    public Message getMessageById(@PathVariable String id) {
        if(id == null) {
            throw new IllegalStateException("Id must not be null");
        }
        return messageService.getMessage(id);
    }

    @PostMapping
    public Message sendMessage(@Valid @RequestBody MessageDto body) {
        return messageService.sendMessage(body.getSenderId(), body.getChannelId(), body.getContent());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable @NotBlank String id)  {
        messageService.deleteMessage(id);
        return ResponseEntity.ok("Message with id: " + id + " was deleted");
    }

    @PutMapping
    public Message editMessage(@RequestBody MessageDto m) {
        if(m == null) {
            throw new IllegalStateException("Message must not be null");
        }
        Message message = new Message(
            m.getId(), 
            m.getChannelId(), 
            m.getSenderId(), 
            m.getContent(), 
            m.getStatus(),
            m.getReadBy(),
            m.getDateSent()
        );
        return messageService.editMessage(message);
    }
}
