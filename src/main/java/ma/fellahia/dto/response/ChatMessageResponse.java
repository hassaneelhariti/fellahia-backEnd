package ma.fellahia.dto.response;

import lombok.Builder;
import lombok.Data;
import ma.fellahia.domain.ChatMessage;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChatMessageResponse {
    private UUID id;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .role(m.getRole())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
