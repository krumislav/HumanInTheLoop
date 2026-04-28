package mk.ukim.finki.humanintheloopllm.service.impl;

import mk.ukim.finki.humanintheloopllm.enums.ReviewStatus;
import mk.ukim.finki.humanintheloopllm.model.ChatMessage;
import mk.ukim.finki.humanintheloopllm.repository.ChatMessageRepository;
import mk.ukim.finki.humanintheloopllm.service.ChatService;
import mk.ukim.finki.humanintheloopllm.service.OpenRouterService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final OpenRouterService openRouterService;

    public ChatServiceImpl(ChatMessageRepository chatMessageRepository,
                           OpenRouterService openRouterService) {
        this.chatMessageRepository = chatMessageRepository;
        this.openRouterService = openRouterService;
    }

    @Override
    public ChatMessage sendMessage(String prompt, String model) {
        String response = openRouterService.ask(prompt, model);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserPrompt(prompt);
        chatMessage.setAssistantResponse(response);
        chatMessage.setModelName(model);
        chatMessage.setStatus(ReviewStatus.PENDING);

        return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAllByOrderByCreatedAtAsc();
    }

    @Override
    public ChatMessage findById(Long id) {
        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat message not found"));
    }

    @Override
    public ChatMessage approve(Long id) {
        ChatMessage msg = findById(id);
        msg.setStatus(ReviewStatus.APPROVED);
        msg.setReviewedAt(LocalDateTime.now());

        return chatMessageRepository.save(msg);
    }

    @Override
    public ChatMessage reject(Long id) {
        ChatMessage msg = findById(id);
        msg.setStatus(ReviewStatus.REJECTED);
        msg.setReviewedAt(LocalDateTime.now());
        return chatMessageRepository.save(msg);
    }

    @Override
    public ChatMessage correct(Long id, String correctedResponse) {
        ChatMessage msg = findById(id);
        msg.setCorrectedResponse(correctedResponse);
        msg.setStatus(ReviewStatus.CORRECTED);
        msg.setReviewedAt(LocalDateTime.now());

        return chatMessageRepository.save(msg);
    }
}
