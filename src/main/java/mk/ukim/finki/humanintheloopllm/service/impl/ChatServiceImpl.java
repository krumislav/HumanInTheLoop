package mk.ukim.finki.humanintheloopllm.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.humanintheloopllm.enums.ReviewStatus;
import mk.ukim.finki.humanintheloopllm.model.ChatMessage;
import mk.ukim.finki.humanintheloopllm.model.ChatSession;
import mk.ukim.finki.humanintheloopllm.model.PaperChunk;
import mk.ukim.finki.humanintheloopllm.model.User;
import mk.ukim.finki.humanintheloopllm.repository.ChatMessageRepository;
import mk.ukim.finki.humanintheloopllm.repository.ChatSessionRepository;
import mk.ukim.finki.humanintheloopllm.repository.PaperChunkRepository;
import mk.ukim.finki.humanintheloopllm.service.ChatService;
import mk.ukim.finki.humanintheloopllm.web.dto.DashboardStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final PaperChunkRepository paperChunkRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Override
    public ChatSession createNewSession(User user) {
        ChatSession session = new ChatSession();
        session.setTitle("New Chat");
        session.setUser(user);
        return chatSessionRepository.save(session);
    }

    @Override
    public ChatSession getSessionById(Long sessionId) {
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    @Override
    public List<ChatSession> getAllSessions() {
        return chatSessionRepository.findAllByOrderByUpdatedAtDesc();
    }

    @Override
    public List<ChatSession> getSessionsByUser(User user) {
        return chatSessionRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    @Override
    public void sendPromptAndSaveResponse(Long sessionId, String userPrompt, String modelName) {
        try {
            ChatSession session = getSessionById(sessionId);

            // Пребарај релевантни chunks од научните трудови
            List<PaperChunk> relevantChunks = findRelevantChunks(userPrompt);

            // Изгради prompt (со или без контекст)
            String finalPrompt;
            if (!relevantChunks.isEmpty()) {
                String context = buildContextFromChunks(relevantChunks);
                finalPrompt = buildPromptWithContext(context, userPrompt);
            } else {
                finalPrompt = userPrompt; // Директно прашање ако нема chunks
            }

            // Прати до LLM
            String assistantResponse = callOpenRouterAPI(finalPrompt, modelName);

            ChatMessage message = new ChatMessage();
            message.setUserPrompt(userPrompt);
            message.setAssistantResponse(assistantResponse);
            message.setModelName(modelName);
            message.setStatus(ReviewStatus.PENDING);
            message.setCreatedAt(LocalDateTime.now());
            message.setChatSession(session);

            chatMessageRepository.save(message);

            // Auto-rename session based on first message
            if (session.getTitle().equals("New Chat")) {
                String title = userPrompt.length() > 40
                        ? userPrompt.substring(0, 40) + "..."
                        : userPrompt;
                session.setTitle(title);
                session.setUpdatedAt(LocalDateTime.now());
                chatSessionRepository.save(session);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get response from LLM: " + e.getMessage());
        }
    }

    private List<PaperChunk> findRelevantChunks(String query) {
        System.out.println("========== SEARCH DEBUG ==========");
        System.out.println("Query: " + query);

        // Подели го query-то и филтрирај stopwords
        String[] allWords = query.toLowerCase().split("\\s+");

        // Филтрирај помошни зборови (stopwords)
        List<String> stopwords = List.of("tell", "me", "about", "what", "is", "the", "a", "an", "how", "why", "when", "where", "at", "in", "on", "to", "of", "for", "with");
        List<String> keywords = new java.util.ArrayList<>();

        for (String word : allWords) {
            // Отстрани интерпункција
            String cleanWord = word.replaceAll("[^a-z0-9]", "");
            if (!stopwords.contains(cleanWord) && cleanWord.length() > 2) {
                keywords.add(cleanWord);
            }
        }

        System.out.println("Keywords (after filtering): " + String.join(", ", keywords));

        List<PaperChunk> allChunks = paperChunkRepository.findAll();
        System.out.println("Total chunks in database: " + allChunks.size());

        List<PaperChunk> relevantChunks = allChunks.stream()
                .filter(chunk -> {
                    String content = chunk.getContent().toLowerCase();
                    for (String keyword : keywords) {
                        if (content.contains(keyword)) {
                            System.out.println("✓ Found keyword '" + keyword + "' in chunk ID: " + chunk.getId());
                            return true;
                        }
                    }
                    return false;
                })
                .limit(2)
                .toList();

        System.out.println("Relevant chunks found: " + relevantChunks.size());
        System.out.println("==================================");

        return relevantChunks;
    }
    private String buildContextFromChunks(List<PaperChunk> chunks) {
        StringBuilder context = new StringBuilder();
        context.append("Relevant extracts from scientific papers:\n\n");

        for (int i = 0; i < chunks.size(); i++) {
            PaperChunk chunk = chunks.get(i);
            // Скрати го chunk-от на 300 карактери за да не е предолг prompt
            String shortContent = chunk.getContent().length() > 300
                    ? chunk.getContent().substring(0, 300) + "..."
                    : chunk.getContent();
            context.append("Extract ").append(i + 1).append(":\n");
            context.append(shortContent).append("\n\n");
        }

        return context.toString();
    }

    private String buildPromptWithContext(String context, String userPrompt) {
        return String.format("""
            You are a scientific assistant. Answer based ONLY on the following information from scientific papers:
            
            %s
            
            Question: %s
            
            IMPORTANT: Answer only based on the information provided above. If there is no relevant information, say "I cannot answer based on the attached papers."
            """, context, userPrompt);
    }

    private String callOpenRouterAPI(String prompt, String model) {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 500, // Намалено за да се избегне token limit
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            return "Error: No response from API";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public List<ChatMessage> getMessagesBySession(Long sessionId) {
        ChatSession session = getSessionById(sessionId);
        return chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void approveMessage(Long id) {
        ChatMessage message = chatMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(ReviewStatus.APPROVED);
        message.setReviewedAt(LocalDateTime.now());
        chatMessageRepository.save(message);
    }

    @Override
    public void rejectMessage(Long id) {
        ChatMessage message = chatMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(ReviewStatus.REJECTED);
        message.setReviewedAt(LocalDateTime.now());
        chatMessageRepository.save(message);
    }

    @Override
    public void correctMessage(Long id, String correctedResponse) {
        ChatMessage message = chatMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setCorrectedResponse(correctedResponse);
        message.setStatus(ReviewStatus.CORRECTED);
        message.setReviewedAt(LocalDateTime.now());
        chatMessageRepository.save(message);
    }

    @Override
    public void renameSession(Long sessionId, String newTitle) {
        ChatSession session = getSessionById(sessionId);
        session.setTitle(newTitle);
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
    }

    @Override
    public DashboardStats getDashboardStats() {
        long totalMessages = chatMessageRepository.count();
        long approvedCount = chatMessageRepository.countByStatus(ReviewStatus.APPROVED);
        long rejectedCount = chatMessageRepository.countByStatus(ReviewStatus.REJECTED);
        long correctedCount = chatMessageRepository.countByStatus(ReviewStatus.CORRECTED);
        long pendingCount = chatMessageRepository.countByStatus(ReviewStatus.PENDING);

        return new DashboardStats(totalMessages, approvedCount, rejectedCount, correctedCount, pendingCount);
    }
}