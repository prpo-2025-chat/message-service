package com.prpo.chat.message.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import com.prpo.chat.message.entity.Message;
import com.prpo.chat.message.entity.MessageStatus;
import com.prpo.chat.message.service.MessageService;

@WebMvcTest(MessageController.class)
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Test
    void getMessagesForChannel_returnsServiceResponse() throws Exception {
        Message message = new Message(
            "msg-1",
            "channel-1",
            "user-1",
            "hello",
            MessageStatus.SENT,
            Set.of("user-1"),
            new Date(1704067200000L)
        );

        Page<Message> page = new PageImpl<>(List.of(message), PageRequest.of(0, 20), 1);

        when(messageService.getConversation("channel-1", 0, 20))
            .thenReturn(page);

        mockMvc.perform(get("/message")
                .param("channelId", "channel-1")
                .param("pageNo", "0")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value("msg-1"))
            .andExpect(jsonPath("$.content[0].channelId").value("channel-1"))
            .andExpect(jsonPath("$.content[0].senderId").value("user-1"))
            .andExpect(jsonPath("$.content[0].content").value("hello"))
            .andExpect(jsonPath("$.content[0].status").value("SENT"));
    }
}
