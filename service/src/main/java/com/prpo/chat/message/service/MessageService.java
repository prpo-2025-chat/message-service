package com.prpo.chat.message.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.prpo.chat.message.entity.Message;
import com.prpo.chat.message.repository.MessageRepository;

@Service
public class MessageService {

    private final MessageRepository repo;
    private final WebClient encryptionWebClient;

    public MessageService(MessageRepository repo, WebClient encryptionWebClient) {
        this.repo = repo;
        this.encryptionWebClient = encryptionWebClient;
    }

    public Message sendMessage(@NonNull String senderId, @NonNull String receiverId, @NonNull String content) {
        String encryptedContent = encryptContent(content);
        Message m = new Message(senderId, receiverId, encryptedContent);
        return repo.save(m);
    }

    public Message editMessage(@NonNull Message message) {
        String plainContent = message.getContent();
        if (plainContent != null) {
            message.setContent(encryptContent(plainContent));
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

        List<String> decryptedContent = decryptContentBatch(encryptedContent);

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
            String decryptedContent = decryptContent(encryptedContent);
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

    private String encryptContent(@NonNull String plainText) {
        return encryptionWebClient.post()
            .uri("")
            .bodyValue(plainText)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    private String decryptContent(@NonNull String content) {
        return encryptionWebClient.post()
            .uri("/decryption")
            .bodyValue(content)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    private List<String> decryptContentBatch(@NonNull List<String> content) {
        return encryptionWebClient.post()
            .uri("/decryption/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(content)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
            .block();
    }
}
