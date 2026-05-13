package mk.ukim.finki.humanintheloopllm.repository;

import mk.ukim.finki.humanintheloopllm.enums.ReviewStatus;
import mk.ukim.finki.humanintheloopllm.model.ChatMessage;
import mk.ukim.finki.humanintheloopllm.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByOrderByCreatedAtAsc();
    List<ChatMessage> findAllByOrderByCreatedAtDesc();
    List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession);
    long countByStatus(ReviewStatus status);

    Optional<ChatMessage> findFirstByUserPromptIgnoreCaseAndStatusIn(String userPrompt, List<ReviewStatus> statuses);
}