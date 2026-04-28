package mk.ukim.finki.humanintheloopllm.service;

import mk.ukim.finki.humanintheloopllm.model.ChatMessage;

import java.util.List;

public interface ChatService {
    ChatMessage sendMessage(String prompt, String model);

    List<ChatMessage> getAllMessages();

    ChatMessage findById(Long id);

    ChatMessage approve(Long id);

    ChatMessage reject(Long id);

    ChatMessage correct(Long id, String correctedResponse);
}
