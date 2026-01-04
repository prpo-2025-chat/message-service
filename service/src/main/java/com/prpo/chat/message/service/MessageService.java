package com.prpo.chat.message.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.prpo.chat.message.client.EncryptionClient;
import com.prpo.chat.message.client.MediaClient;
import com.prpo.chat.message.client.NotificationClient;
import com.prpo.chat.message.client.PresenceClient;
import com.prpo.chat.message.client.SearchClient;
import com.prpo.chat.message.client.dto.IndexMessageRequestDto;
import com.prpo.chat.message.client.dto.MessageReceivedNotificationRequest;
import com.prpo.chat.message.entity.MediaAttachment;
import com.prpo.chat.message.entity.Message;
import com.prpo.chat.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository repo;
    private final EncryptionClient encryptionClient;
    private final NotificationClient notificationClient;
    private final PresenceClient presenceClient;
    private final SearchClient searchClient;
    private final MediaClient mediaClient;

    public Message sendMessage(@NonNull String senderId, @NonNull String channelId, @NonNull String content, List<MultipartFile> files) {

        String encryptedContent = encryptionClient.encrypt(content);
        Message m = new Message(channelId, senderId, encryptedContent);
        m.setMedia(uploadFiles(files, senderId));

        Message saved = repo.save(m);

        MessageReceivedNotificationRequest notificationRequest = new MessageReceivedNotificationRequest();
        notificationRequest.setMessageId(saved.getId());
        notificationRequest.setSenderId(senderId);
        notificationRequest.setChannelId(channelId);
        notificationRequest.setText(firstThreeWords(content));

        notificationClient.notifyMessageReceived(notificationRequest);

        presenceClient.setUserOnline(senderId);

        IndexMessageRequestDto indexMessageDto = new IndexMessageRequestDto();
        indexMessageDto.setId(saved.getId());
        indexMessageDto.setChannelId(channelId);
        indexMessageDto.setSenderId(senderId);
        indexMessageDto.setContent(content);
        indexMessageDto.setDateSent(saved.getDateSent());

        searchClient.indexMessage(indexMessageDto);

        saved.setContent(content);
        saved.setMedia(m.getMedia());

        return saved;
    }

    public Message editMessage(@NonNull Message message) {
        String plainContent = message.getContent();
        if (plainContent != null) {
            message.setContent(encryptionClient.encrypt(plainContent));
        }
        return repo.save(message);
    }

    public Page<Message> getConversation(String channelId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Message> page = repo.findByChannelIdOrderByDateSentDesc(channelId, pageable);

        List<Message> messages = page.getContent();
        List<String> encryptedContent = messages.stream()
            .map(Message::getContent)
            .toList();

        if(encryptedContent == null) {
            throw new IllegalStateException("Null content");
        }

        List<String> decryptedContent = encryptionClient.decryptBatch(encryptedContent);

        if (decryptedContent.size() != messages.size()) {
            throw new IllegalStateException(
                "Batch decrypt size mismatch. Expected length: " + messages.size() + 
                ", recieved length: " + decryptedContent.size() + 
                ", recieved content: " + decryptedContent.toString()
            );
        }

        List<Message> messageList = new ArrayList<>(messages.size());
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            String decrypted = decryptedContent.get(i);
            m.setContent(decrypted);

            messageList.add(m);
        }


        return new PageImpl<>(
            messageList,
            page.getPageable(),
            page.getTotalElements()
        );
    }

    public Message getMessage(@NonNull String id) {
        Message m = repo.findById(id).orElseThrow();
        String encryptedContent = m.getContent();
        if (encryptedContent != null) {
            String decryptedContent = encryptionClient.decrypt(encryptedContent);
            m.setContent(decryptedContent);
        }
        return m;
    }

    public void deleteMessage(@NonNull String id) {
        repo.deleteById(id);
    }

    public List<Message> getInbox(String userId) {
        // TODO: connect with UserService or ServerService to get list of all channelIds user is in
        List<String> channelIdsForUserId = new ArrayList<>(3);
        for(int i = 0; i < 3; i++) {
            channelIdsForUserId.set(i, i+"");
        }
        return repo.findByChannelIdInAndReadByNotContaining(channelIdsForUserId, userId);
    }

    public static String firstThreeWords(String input) {
        if (input == null || input.isBlank()) return input;

        String[] parts = input.trim().split("\\s+");
        int limit = Math.min(3, parts.length);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            if (i > 0) result.append(" ");
            result.append(parts[i]);
        }
        return result.toString();
    }

    private List<MediaAttachment> uploadFiles(List<MultipartFile> files, String uploaderId) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<MediaAttachment> uploaded = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            try {
                MediaAttachment media = mediaClient.upload(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    uploaderId,
                    resolveMediaType(file.getContentType())
                );
                if (media != null) {
                    uploaded.add(media);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file content for upload", e);
            }
        }
        return uploaded;
    }

    private String resolveMediaType(String contentType) {
        if (contentType == null) {
            return "DOCUMENT";
        }
        String lower = contentType.toLowerCase();
        if (lower.startsWith("image/")) return "IMAGE";
        if (lower.startsWith("video/")) return "VIDEO";
        if (lower.startsWith("audio/")) return "AUDIO";
        return "DOCUMENT";
    }

}
