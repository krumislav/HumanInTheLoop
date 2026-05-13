package mk.ukim.finki.humanintheloopllm.repository;

import mk.ukim.finki.humanintheloopllm.model.ChatSession;
import mk.ukim.finki.humanintheloopllm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findAllByOrderByUpdatedAtDesc();

    List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);
}