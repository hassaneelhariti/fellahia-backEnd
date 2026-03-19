package ma.fellahia.repository;

import ma.fellahia.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(UUID userId);

    /**
     * Returns the last N messages (most recent first) for context window.
     * Call Collections.reverse() after fetching to get chronological order.
     */
    List<ChatMessage> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);

    void deleteByUserId(UUID userId);
}
