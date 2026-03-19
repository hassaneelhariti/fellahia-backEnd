package ma.fellahia.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fellahia.dto.request.ChatRequest;
import ma.fellahia.dto.response.ChatMessageResponse;
import ma.fellahia.security.UserDetailsImpl;
import ma.fellahia.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fellah/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * GET /api/fellah/chat/history
     * Returns all chat messages for the authenticated Fellah, oldest first.
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(chatService.getHistory(userDetails.getId()));
    }

    /**
     * POST /api/fellah/chat/send
     * Sends a message to the AI and returns the assistant's response.
     */
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> send(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(
                chatService.sendMessage(userDetails.getId(), request.getMessage()));
    }

    /**
     * DELETE /api/fellah/chat/history
     * Clears the entire chat history for the Fellah.
     */
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatService.clearHistory(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
