package mk.ukim.finki.humanintheloopllm.repository;

import mk.ukim.finki.humanintheloopllm.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    List<ChatMessage> findAllByOrderByCreatedAtAsc();
}
