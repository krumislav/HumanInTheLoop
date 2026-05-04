package mk.ukim.finki.humanintheloopllm.repository;

import mk.ukim.finki.humanintheloopllm.enums.ReviewStatus;
import mk.ukim.finki.humanintheloopllm.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    List<ChatMessage> findAllByOrderByCreatedAtAsc();
    Optional<ChatMessage> findFirstByUserPromptIgnoreCaseAndStatus(String userPrompt, ReviewStatus status);
}
