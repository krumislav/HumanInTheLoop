package mk.ukim.finki.humanintheloopllm.service;

import mk.ukim.finki.humanintheloopllm.model.ChatMessage;
import mk.ukim.finki.humanintheloopllm.model.ChatSession;
import mk.ukim.finki.humanintheloopllm.model.User;
import mk.ukim.finki.humanintheloopllm.web.dto.DashboardStats;

import java.util.List;

public interface ChatService {
    ChatSession createNewSession(User user);
    ChatSession getSessionById(Long sessionId);
    List<ChatSession> getAllSessions();
    List<ChatSession> getSessionsByUser(User user);

    void sendPromptAndSaveResponse(Long sessionId, String userPrompt, String modelName);
    List<ChatMessage> getMessagesBySession(Long sessionId);
    List<ChatMessage> getAllMessages();

    void approveMessage(Long id);
    void rejectMessage(Long id);
    void correctMessage(Long id, String correctedResponse);
    void renameSession(Long sessionId, String newTitle);

    DashboardStats getDashboardStats();
}