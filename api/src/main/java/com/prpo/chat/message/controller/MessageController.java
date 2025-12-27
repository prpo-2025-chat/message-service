package com.prpo.chat.message.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prpo.chat.message.dto.MessageDto;
import com.prpo.chat.message.entity.Message;
import com.prpo.chat.message.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;



@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(
        summary = "Get messages for a channel",
        description = "Returns a paginated list of messages for the given channel"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Messages retrieved successfully",
            content = @Content(schema = @Schema(implementation = Message.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping
    public Page<Message> getMessagesForChannel(
        @Parameter(description = "Channel ID", required = true)
        @RequestParam("channelId") String channelId,

        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam("pageNo") int pageNo,

        @Parameter(description = "Page size", example = "20")
        @RequestParam("pageSize") int pageSize
    ) {
        return messageService.getConversation(channelId, pageNo, pageSize);
    }

    @Operation(
        summary = "Get inbox for user",
        description = "Returns the latest messages for all channels the user participates in"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inbox retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    @GetMapping("/inbox")
    public List<Message> getInboxForUserId(
        @Parameter(description = "User ID", required = true)
        @RequestParam("userId") String userId
    ) {
        return messageService.getInbox(userId);
    }

    @Operation(
        summary = "Get message by ID",
        description = "Returns a single message by its unique identifier"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message found"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @GetMapping("/{id}")
    public Message getMessageById(
        @Parameter(description = "Message ID", required = true)
        @PathVariable String id
    ) {
        if (id == null) {
            throw new IllegalStateException("Id must not be null");
        }
        return messageService.getMessage(id);
    }

    @Operation(
        summary = "Send a message",
        description = "Creates and sends a new message to a channel"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid message payload")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Message sendMessage(
        @Parameter(description = "Message payload", required = true)
        @Valid @RequestPart("payload") MessageDto body,
        @RequestPart(name = "files", required = false) List<MultipartFile> files
    ) {
        return messageService.sendMessage(
            body.getSenderId(),
            body.getChannelId(),
            body.getContent(),
            files
        );
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Message sendMessageJson(
        @Parameter(description = "Message payload", required = true)
        @Valid @RequestBody MessageDto body
    ) {
        return messageService.sendMessage(
            body.getSenderId(),
            body.getChannelId(),
            body.getContent(),
            List.of()
        );
    }

    @Operation(
        summary = "Delete a message",
        description = "Deletes a message by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(
        @Parameter(description = "Message ID", required = true)
        @PathVariable @NotBlank String id
    ) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok("Message with id: " + id + " was deleted");
    }

    @Operation(
        summary = "Edit a message",
        description = "Updates the content or status of an existing message"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid message payload"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping
    public Message editMessage(
        @Parameter(description = "Updated message payload", required = true)
        @RequestBody MessageDto m
    ) {
        if (m == null) {
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
        message.setMedia(m.getMedia());
        return messageService.editMessage(message);
    }
}

