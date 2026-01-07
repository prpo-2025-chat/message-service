package com.prpo.chat.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import com.prpo.chat.message.client.EncryptionClient;
import com.prpo.chat.message.client.MediaClient;
import com.prpo.chat.message.client.NotificationClient;
import com.prpo.chat.message.client.PresenceClient;
import com.prpo.chat.message.client.SearchClient;
import com.prpo.chat.message.client.dto.IndexMessageRequestDto;
import com.prpo.chat.message.client.dto.MessageReceivedNotificationRequest;
import com.prpo.chat.message.entity.MediaAttachment;
import com.prpo.chat.message.entity.Message;
import com.prpo.chat.message.entity.MessageStatus;
import com.prpo.chat.message.repository.MessageRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository repo;

    @Mock
    private EncryptionClient encryptionClient;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private PresenceClient presenceClient;

    @Mock
    private SearchClient searchClient;

    @Mock
    private MediaClient mediaClient;

    @InjectMocks
    private MessageService service;

    @Test
    void sendMessage_encrypts_saves_and_notifies() {
        String senderId = "user-1";
        String channelId = "channel-1";
        String content = "hello world again extra";

        MockMultipartFile file = new MockMultipartFile(
            "files",
            "test.png",
            "image/png",
            "abc".getBytes(StandardCharsets.UTF_8)
        );
        MediaAttachment attachment = new MediaAttachment(
            "media-1",
            senderId,
            "test.png",
            "image/png",
            3L,
            new Date(0),
            "http://example.com/file"
        );

        when(encryptionClient.encrypt(content)).thenReturn("enc-content");
        when(mediaClient.upload(any(), eq("test.png"), eq("image/png"), eq(senderId), eq("IMAGE")))
            .thenReturn(attachment);

        Message saved = new Message(channelId, senderId, "enc-content");
        saved.setId("msg-1");
        saved.setDateSent(new Date(1704067200000L));

        when(repo.save(any(Message.class))).thenReturn(saved);

        Message result = service.sendMessage(senderId, channelId, content, List.of(file));

        ArgumentCaptor<Message> savedCaptor = ArgumentCaptor.forClass(Message.class);
        verify(repo).save(savedCaptor.capture());
        Message persisted = savedCaptor.getValue();
        assertEquals("enc-content", persisted.getContent());
        assertEquals(1, persisted.getMedia().size());
        assertEquals(attachment, persisted.getMedia().get(0));

        ArgumentCaptor<MessageReceivedNotificationRequest> notifyCaptor =
            ArgumentCaptor.forClass(MessageReceivedNotificationRequest.class);
        verify(notificationClient).notifyMessageReceived(notifyCaptor.capture());
        MessageReceivedNotificationRequest notify = notifyCaptor.getValue();
        assertEquals("msg-1", notify.getMessageId());
        assertEquals(senderId, notify.getSenderId());
        assertEquals(channelId, notify.getChannelId());
        assertEquals("hello world again", notify.getText());

        ArgumentCaptor<IndexMessageRequestDto> indexCaptor =
            ArgumentCaptor.forClass(IndexMessageRequestDto.class);
        verify(searchClient).indexMessage(indexCaptor.capture());
        IndexMessageRequestDto indexDto = indexCaptor.getValue();
        assertEquals("msg-1", indexDto.getId());
        assertEquals(senderId, indexDto.getSenderId());
        assertEquals(channelId, indexDto.getChannelId());
        assertEquals(content, indexDto.getContent());
        assertEquals(saved.getDateSent(), indexDto.getDateSent());

        verify(presenceClient).setUserOnline(senderId);
        verify(mediaClient).upload(any(), eq("test.png"), eq("image/png"), eq(senderId), eq("IMAGE"));

        assertEquals(content, result.getContent());
        assertEquals(1, result.getMedia().size());
    }

    @Test
    void editMessage_encryptsContentBeforeSaving() {
        Message message = new Message(
            "channel-1",
            "user-1",
            "hello"
        );

        when(encryptionClient.encrypt("hello")).thenReturn("enc-hello");
        when(repo.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message result = service.editMessage(message);

        assertEquals("enc-hello", result.getContent());
        verify(repo).save(message);
    }

    @Test
    void getConversation_decryptsBatchContent() {
        Message first = new Message("channel-1", "user-1", "enc-1");
        first.setId("msg-1");
        first.setStatus(MessageStatus.SENT);
        first.setReadBy(Set.of("user-1"));

        Message second = new Message("channel-1", "user-2", "enc-2");
        second.setId("msg-2");
        second.setStatus(MessageStatus.DELIVERED);
        second.setReadBy(Set.of("user-2"));

        Page<Message> page = new PageImpl<>(
            List.of(first, second),
            PageRequest.of(0, 2),
            2
        );

        when(repo.findByChannelIdOrderByDateSentDesc(eq("channel-1"), any(Pageable.class)))
            .thenReturn(page);
        when(encryptionClient.decryptBatch(anyList()))
            .thenReturn(List.of("plain-1", "plain-2"));

        Page<Message> result = service.getConversation("channel-1", 0, 2);

        assertEquals("plain-1", result.getContent().get(0).getContent());
        assertEquals("plain-2", result.getContent().get(1).getContent());

        ArgumentCaptor<List<String>> decryptCaptor = ArgumentCaptor.forClass(List.class);
        verify(encryptionClient).decryptBatch(decryptCaptor.capture());
        assertEquals(List.of("enc-1", "enc-2"), decryptCaptor.getValue());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).findByChannelIdOrderByDateSentDesc(eq("channel-1"), pageableCaptor.capture());
        assertEquals(PageRequest.of(0, 2), pageableCaptor.getValue());
    }

    @Test
    void getMessage_decryptsContent() {
        Message message = new Message("channel-1", "user-1", "enc-1");
        message.setId("msg-1");

        when(repo.findById("msg-1")).thenReturn(Optional.of(message));
        when(encryptionClient.decrypt("enc-1")).thenReturn("plain-1");

        Message result = service.getMessage("msg-1");

        assertEquals("plain-1", result.getContent());
        verify(encryptionClient).decrypt("enc-1");
    }

    @Test
    void deleteMessage_delegatesToRepository() {
        service.deleteMessage("msg-1");

        verify(repo).deleteById("msg-1");
    }

    @Test
    void getInbox_throwsWhileBuildingChannelList() {
        assertThrows(IndexOutOfBoundsException.class, () -> service.getInbox("user-1"));
        verifyNoInteractions(repo);
    }

    @Test
    void firstThreeWords_returnsExpectedSegments() {
        assertEquals("one two three", MessageService.firstThreeWords("one two three four"));
        assertEquals("one two", MessageService.firstThreeWords("one two"));
        assertEquals("", MessageService.firstThreeWords(""));
        assertNull(MessageService.firstThreeWords(null));
    }
}
