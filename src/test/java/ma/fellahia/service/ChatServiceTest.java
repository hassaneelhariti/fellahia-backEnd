package ma.fellahia.service;

import ma.fellahia.domain.ChatMessage;
import ma.fellahia.dto.response.ChatMessageResponse;
import ma.fellahia.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ChatMessageRepository messageRepository;
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks ChatService chatService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void sendMessage_shouldPersistUserAndAssistantMessages() {
        String userText = "ما هي حقوقي القانونية؟";
        String aiReply = "لديك الحق في...";

        ChatMessage savedMsg = ChatMessage.builder()
                .userId(userId).role("assistant").content(aiReply).build();

        when(messageRepository.save(any(ChatMessage.class))).thenReturn(savedMsg);
        when(messageRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.messages(anyList())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(aiReply);

        ChatMessageResponse response = chatService.sendMessage(userId, userText);

        assertThat(response).isNotNull();
        verify(messageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_shouldBuildHistoryWithMixedRoles() {
        ChatMessage userMsg = ChatMessage.builder()
                .userId(userId).role("user").content("سؤال").build();
        ChatMessage assistantMsg = ChatMessage.builder()
                .userId(userId).role("assistant").content("جواب").build();
        ChatMessage savedMsg = ChatMessage.builder()
                .userId(userId).role("assistant").content("رد").build();

        when(messageRepository.save(any(ChatMessage.class))).thenReturn(savedMsg);
        when(messageRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(assistantMsg, userMsg));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.messages(Collections.singletonList(any()))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("رد");

        chatService.sendMessage(userId, "سؤال جديد");

        verify(chatClient).prompt();
    }

    @Test
    void getHistory_shouldReturnOrderedMessages() {
        ChatMessage msg1 = ChatMessage.builder()
                .userId(userId).role("user").content("مرحبا").build();
        ChatMessage msg2 = ChatMessage.builder()
                .userId(userId).role("assistant").content("أهلا").build();

        when(messageRepository.findByUserIdOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(msg1, msg2));

        List<ChatMessageResponse> history = chatService.getHistory(userId);

        assertThat(history).hasSize(2);
    }

    @Test
    void getHistory_shouldReturnEmptyList_whenNoMessages() {
        when(messageRepository.findByUserIdOrderByCreatedAtAsc(userId))
                .thenReturn(List.of());

        List<ChatMessageResponse> history = chatService.getHistory(userId);

        assertThat(history).isEmpty();
    }

    @Test
    void clearHistory_shouldDeleteAllUserMessages() {
        doNothing().when(messageRepository).deleteByUserId(userId);
        chatService.clearHistory(userId);
        verify(messageRepository).deleteByUserId(userId);
    }
}
