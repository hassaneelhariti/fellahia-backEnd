package ma.fellahia.service;

import lombok.RequiredArgsConstructor;
import ma.fellahia.domain.ChatMessage;
import ma.fellahia.dto.response.ChatMessageResponse;
import ma.fellahia.repository.ChatMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMessageRepository messageRepository;

    /**
     * Sends a user message to the AI, persists both sides, and returns the assistant reply.
     */
    @Transactional
    public ChatMessageResponse sendMessage(UUID userId, String userText) {

        // 1. Persist user message first
        ChatMessage userMsg = ChatMessage.builder()
                .userId(userId)
                .role("user")
                .content(userText)
                .build();
        messageRepository.save(userMsg);

        // 2. Build conversation history (last 20 msgs, reversed to chronological)
        List<ChatMessage> history = new ArrayList<>(
                messageRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId));
        history = history.reversed();

        List<Message> aiMessages = history.stream()
                .map(m -> m.getRole().equals("user")
                        ? (Message) new UserMessage(m.getContent())
                        : new AssistantMessage(m.getContent()))
                .toList();

        // 3. Call AI
        String aiReply = chatClient.prompt()
                .messages(aiMessages)
                .call()
                .content();

        // 4. Persist assistant reply
        ChatMessage assistantMsg = ChatMessage.builder()
                .userId(userId)
                .role("assistant")
                .content(aiReply)
                .build();
        messageRepository.save(assistantMsg);

        return ChatMessageResponse.from(assistantMsg);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(UUID userId) {
        return messageRepository.findByUserIdOrderByCreatedAtAsc(userId)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public void clearHistory(UUID userId) {
        messageRepository.deleteByUserId(userId);
    }
}
